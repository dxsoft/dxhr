$ErrorActionPreference = 'Stop'

Write-Output '--- app.js without session (first 80 chars) ---'
try {
    $raw = (Invoke-WebRequest -Uri 'http://127.0.0.1:8081/app.js?v=20260713-dashboard-init-fix' -UseBasicParsing -MaximumRedirection 0).Content
} catch {
    $resp = $_.Exception.Response
    if ($resp -and $resp.StatusCode.value__ -eq 302) {
        Write-Output ('redirect: ' + $resp.Headers['Location'])
    } else {
        Write-Output $_.Exception.Message
    }
    $raw = $null
}
if ($raw) {
    Write-Output $raw.Substring(0, [Math]::Min(120, $raw.Length))
}

$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Invoke-WebRequest -Uri 'http://127.0.0.1:8081/login' -Method POST -WebSession $session -Body @{ username = 'admin'; password = 'admin123' } -UseBasicParsing | Out-Null

Write-Output '--- app.js with session (first 80 chars) ---'
$js = (Invoke-WebRequest -Uri 'http://127.0.0.1:8081/app.js?v=20260713-dashboard-init-fix' -WebSession $session -UseBasicParsing).Content
Write-Output $js.Substring(0, [Math]::Min(120, $js.Length))
Write-Output ('starts with const state: ' + $js.StartsWith('const state'))

Write-Output '--- response headers with session ---'
(Invoke-WebRequest -Uri 'http://127.0.0.1:8081/app.js?v=20260713-dashboard-init-fix' -WebSession $session -UseBasicParsing).Headers['Cache-Control']
