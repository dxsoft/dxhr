param(
    [string]$HostName = "127.0.0.1",
    [int]$Port = 3306,
    [string]$User = "root",
    [string]$Password = "dx262105",
    [string]$Database = "gzjsgl"
)

$ErrorActionPreference = "Stop"
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (-not (Test-Path $mysql)) {
    throw "MySQL client not found: $mysql"
}

$ops = Join-Path $PSScriptRoot "."
$auditSql = Join-Path $ops "migrate-personnel-approval-actors.sql"
$legacySql = Join-Path $ops "migrate-personnel-approval-actors-legacy.sql"

function Invoke-MysqlFile {
    param([string]$SqlPath)
    $path = ($SqlPath -replace '\\', '/')
    & $mysql --default-character-set=utf8mb4 --host=$HostName --port=$Port "-u$User" "-p$Password" $Database -e "source $path"
}

Write-Host "========== local $Database @ ${HostName}:$Port audit backfill =========="
Invoke-MysqlFile $auditSql
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "========== local $Database legacy fallback =========="
Invoke-MysqlFile $legacySql
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$stats = @"
SELECT CONCAT('dryjbxx approved shr=', COUNT(*)) FROM dryjbxx WHERE TRIM(COALESCE(bbz,''))='审批通过' AND shr IS NOT NULL;
SELECT CONCAT('dryzwbh approved shr=', COUNT(*)) FROM dryzwbh WHERE TRIM(COALESCE(bbz,''))='审批通过' AND shr IS NOT NULL;
SELECT CONCAT('dxl approved shr=', COUNT(*)) FROM dxl WHERE TRIM(COALESCE(bbz,''))='审批通过' AND shr IS NOT NULL;
SELECT CONCAT('dndkh approved shr=', COUNT(*)) FROM dndkh WHERE TRIM(COALESCE(bbz,''))='审批通过' AND shr IS NOT NULL;
"@
Write-Host "========== stats =========="
$stats | & $mysql --default-character-set=utf8mb4 --host=$HostName --port=$Port "-u$User" "-p$Password" $Database -N
Write-Host "done"
