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
$bulkSql = Join-Path $ops "bulk-set-personnel-approved.sql"
$legacySql = Join-Path $ops "migrate-personnel-approval-actors-legacy.sql"

function Invoke-MysqlFile {
    param([string]$SqlPath)
    $path = ($SqlPath -replace '\\', '/')
    & $mysql --default-character-set=utf8mb4 --host=$HostName --port=$Port "-u$User" "-p$Password" $Database -e "source $path"
}

function Invoke-MysqlQuery {
    param([string]$Query)
    & $mysql --default-character-set=utf8mb4 --host=$HostName --port=$Port "-u$User" "-p$Password" $Database -N -e $Query
}

Write-Host "========== before (non-approved counts) $Database @ ${HostName}:$Port =========="
$before = @"
SELECT CONCAT('dryjbxx other=', COUNT(*)) FROM dryjbxx WHERE TRIM(COALESCE(bbz,'')) <> '审批通过';
SELECT CONCAT('dryzwbh other=', COUNT(*)) FROM dryzwbh WHERE TRIM(COALESCE(bbz,'')) <> '审批通过';
SELECT CONCAT('dxl other=', COUNT(*)) FROM dxl WHERE TRIM(COALESCE(bbz,'')) <> '审批通过';
SELECT CONCAT('dndkh other=', COUNT(*)) FROM dndkh WHERE TRIM(COALESCE(bbz,'')) <> '审批通过';
SELECT CONCAT('hjxx other=', COUNT(*)) FROM hjxx WHERE TRIM(COALESCE(bbz,'')) <> '审批通过';
SELECT CONCAT('jx other=', COUNT(*)) FROM jx WHERE TRIM(COALESCE(bbz,'')) <> '审批通过';
"@
Invoke-MysqlQuery $before

Write-Host "========== bulk set bbz -> 审批通过 =========="
Invoke-MysqlFile $bulkSql
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "========== legacy actor backfill =========="
Invoke-MysqlFile $legacySql
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "========== after =========="
$after = @"
SELECT CONCAT('dryjbxx approved=', COUNT(*)) FROM dryjbxx WHERE TRIM(bbz)='审批通过';
SELECT CONCAT('dryzwbh approved=', COUNT(*)) FROM dryzwbh WHERE TRIM(bbz)='审批通过';
SELECT CONCAT('dxl approved=', COUNT(*)) FROM dxl WHERE TRIM(bbz)='审批通过';
SELECT CONCAT('dndkh approved=', COUNT(*)) FROM dndkh WHERE TRIM(bbz)='审批通过';
SELECT CONCAT('hjxx approved=', COUNT(*)) FROM hjxx WHERE TRIM(bbz)='审批通过';
SELECT CONCAT('jx approved=', COUNT(*)) FROM jx WHERE TRIM(bbz)='审批通过';
SELECT CONCAT('dryjbxx shr=', COUNT(*)) FROM dryjbxx WHERE shr IS NOT NULL;
"@
Invoke-MysqlQuery $after
Write-Host "done"
