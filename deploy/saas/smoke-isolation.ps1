# Dual-instance SaaS isolation smoke test.
# Usage:
#   $env:RSGZGL_DB_PASSWORD = 'mysql-password'
#   .\smoke-isolation.ps1
param(
    [string]$MysqlPassword = $env:RSGZGL_DB_PASSWORD,
    [string]$MysqlUser = $(if ($env:RSGZGL_DB_USERNAME) { $env:RSGZGL_DB_USERNAME } else { "root" }),
    [string]$MysqlHost = "127.0.0.1",
    [int]$PortA = 18081,
    [int]$PortB = 18082
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
$Work = Join-Path $env:TEMP "rsgzgl-saas-smoke"
$Connector = Get-ChildItem "$env:USERPROFILE\.m2\repository\com\mysql\mysql-connector-j" -Recurse -Filter "mysql-connector-j-*.jar" |
    Where-Object { $_.Name -notmatch "sources|javadoc" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1 -ExpandProperty FullName

if (-not $MysqlPassword) {
    throw "Set RSGZGL_DB_PASSWORD or pass -MysqlPassword"
}
if (-not $Connector) {
    throw "mysql-connector-j not found; run mvn -DskipTests package first"
}

$Jar = Get-ChildItem (Join-Path $Root "target\rsgzgl-*.jar") |
    Where-Object { $_.Name -notmatch "sources|javadoc|original" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if (-not $Jar) {
    Write-Host "==> packaging jar"
    Push-Location $Root
    try {
        & mvn -DskipTests package
        if ($LASTEXITCODE -ne 0) { throw "mvn package failed" }
    } finally {
        Pop-Location
    }
    $Jar = Get-ChildItem (Join-Path $Root "target\rsgzgl-*.jar") |
        Where-Object { $_.Name -notmatch "sources|javadoc|original" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
}
if (-not $Jar) { throw "Spring Boot jar not found" }

if (Test-Path $Work) { Remove-Item $Work -Recurse -Force }
New-Item -ItemType Directory -Force -Path $Work | Out-Null
Copy-Item (Join-Path $PSScriptRoot "tools\MysqlProbe.java") (Join-Path $Work "MysqlProbe.java") -Force
Write-Host "==> compile MysqlProbe"
& javac -cp $Connector (Join-Path $Work "MysqlProbe.java")
if ($LASTEXITCODE -ne 0) { throw "javac failed" }

function Invoke-Mysql {
    param([string[]]$SqlList = @())
    $allArgs = @("-cp", "$Work;$Connector", "MysqlProbe", $MysqlPassword) + $SqlList
    & java @allArgs
    if ($LASTEXITCODE -ne 0) { throw "MySQL failed (check password)" }
}

Write-Host "==> probe MySQL"
Invoke-Mysql

$dbA = "gzjsgl_saas_a"
$dbB = "gzjsgl_saas_b"
Write-Host "==> create databases $dbA / $dbB"
Invoke-Mysql @(
    "CREATE DATABASE IF NOT EXISTS ``$dbA`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
    "CREATE DATABASE IF NOT EXISTS ``$dbB`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
)

$schemaSql = Join-Path $Root "gzjsgl.sql"
if (-not (Test-Path $schemaSql)) {
    throw "schema file missing: $schemaSql"
}
Write-Host "==> load schema into $dbA / $dbB (may take a minute)"
& java -cp "$Work;$Connector" MysqlProbe $MysqlPassword --file $schemaSql $dbA
if ($LASTEXITCODE -ne 0) { throw "schema load failed for $dbA" }
& java -cp "$Work;$Connector" MysqlProbe $MysqlPassword --file $schemaSql $dbB
if ($LASTEXITCODE -ne 0) { throw "schema load failed for $dbB" }

function JdbcUrl([string]$db) {
    "jdbc:mysql://${MysqlHost}:3306/${db}?useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
}

function Start-TenantEnv([string]$Name, [int]$Port, [string]$Db) {
    $log = Join-Path $Work "$Name.log"
    $adminPassword = "saas-$Name-admin"
    $url = JdbcUrl $Db
    $cmd = Join-Path $Work "start-$Name.cmd"
    @"
@echo off
set "PORT=$Port"
set "RSGZGL_DB_URL=$url"
set "RSGZGL_DB_USERNAME=$MysqlUser"
set "RSGZGL_DB_PASSWORD=$MysqlPassword"
set "RSGZGL_ADMIN_USERNAME=admin"
set "RSGZGL_ADMIN_PASSWORD=$adminPassword"
set "RSGZGL_ADMIN_RESET_PASSWORD=true"
set "RSGZGL_SECURITY_INITIALIZE_SCHEMA=true"
set "RSGZGL_LICENSE_ISSUE_ENABLED=false"
set "RSGZGL_LICENSE_HMAC_SECRET=saas-smoke-hmac-$Name"
set "RSGZGL_FORWARD_HEADERS_STRATEGY=framework"
set "RSGZGL_SESSION_COOKIE_SECURE=false"
set "RSGZGL_UKEY_ENABLED=true"
cd /d "$Work"
java -Xms256m -Xmx512m -jar "$($Jar.FullName)" > "$log" 2>&1
"@ | Set-Content -Path $cmd -Encoding ASCII
    $p = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "`"$cmd`"" -PassThru -WindowStyle Hidden
    return @{
        Process = $p
        Log = $log
        Port = $Port
        Name = $Name
        AdminPassword = $adminPassword
    }
}

function Wait-Health([int]$Port, [string]$LogPath, [int]$TimeoutSec = 180) {
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        $body = & curl.exe -s -m 3 "http://127.0.0.1:$Port/actuator/health" 2>$null
        if ($LASTEXITCODE -eq 0 -and "$body" -match "UP") {
            return $body
        }
        Start-Sleep -Seconds 2
    }
    if (Test-Path $LogPath) {
        Write-Host "---- last log lines ($LogPath) ----"
        Get-Content $LogPath -Tail 40
    }
    throw "health check timeout on port $Port"
}

function Login-Cookie([int]$Port, [string]$Password, [string]$CookieJar) {
    if (Test-Path $CookieJar) { Remove-Item $CookieJar -Force }
    & curl.exe -s -m 15 -c $CookieJar -b $CookieJar -X POST "http://127.0.0.1:$Port/login" `
        -H "Content-Type: application/x-www-form-urlencoded" `
        -d "username=admin&password=$Password" -o NUL -w "%{http_code}" | Out-Null
}

function Get-LicenseAuthorized([int]$Port, [string]$CookieJar) {
    $body = & curl.exe -s -m 15 -b $CookieJar "http://127.0.0.1:$Port/api/license/status"
    if ($LASTEXITCODE -ne 0) { throw "license status request failed on $Port" }
    if ($body -match '"authorized"\s*:\s*true') { return $true }
    if ($body -match '"authorized"\s*:\s*false') { return $false }
    # unauthenticated often redirects to login HTML
    if ($body -match "login|Login|DOCTYPE") { throw "not authenticated" }
    throw "unexpected license response: $body"
}

function Stop-Tree([System.Diagnostics.Process]$Proc) {
    if ($null -eq $Proc) { return }
    try {
        & taskkill.exe /PID $Proc.Id /T /F 2>$null | Out-Null
    } catch {}
}

$procA = $null
$procB = $null
try {
    Write-Host "==> start tenant A @$PortA"
    $procA = Start-TenantEnv "saas_a" $PortA $dbA
    Write-Host "==> start tenant B @$PortB"
    $procB = Start-TenantEnv "saas_b" $PortB $dbB

    Write-Host "==> wait health"
    $hA = Wait-Health $PortA $procA.Log
    $hB = Wait-Health $PortB $procB.Log
    Write-Host "A health: $hA"
    Write-Host "B health: $hB"

    $cookieA = Join-Path $Work "cookie-a.txt"
    $cookieB = Join-Path $Work "cookie-b.txt"
    $cookieCross = Join-Path $Work "cookie-cross.txt"

    Login-Cookie $PortA $procA.AdminPassword $cookieA
    Login-Cookie $PortB $procB.AdminPassword $cookieB

    $authA = Get-LicenseAuthorized $PortA $cookieA
    $authB = Get-LicenseAuthorized $PortB $cookieB
    Write-Host ("A license authorized={0}" -f $authA)
    Write-Host ("B license authorized={0}" -f $authB)

    Login-Cookie $PortB $procA.AdminPassword $cookieCross
    $crossOk = $false
    try {
        $null = Get-LicenseAuthorized $PortB $cookieCross
        $crossOk = $true
    } catch {
        $crossOk = $false
    }
    if ($crossOk) {
        throw "isolation failed: tenant A admin password must not login tenant B"
    }

    Write-Host "PASS: dual instances healthy; DB and admin passwords isolated"
}
finally {
    if ($procA) { Stop-Tree $procA.Process }
    if ($procB) { Stop-Tree $procB.Process }
}
