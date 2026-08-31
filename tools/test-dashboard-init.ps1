$ErrorActionPreference = 'Stop'
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
Invoke-WebRequest -Uri 'http://127.0.0.1:8081/login' -Method POST -WebSession $session -Body @{ username = 'admin'; password = 'admin123' } -UseBasicParsing | Out-Null

$me = (Invoke-WebRequest -Uri 'http://127.0.0.1:8081/api/auth/me' -WebSession $session -UseBasicParsing).Content
$menus = (Invoke-WebRequest -Uri 'http://127.0.0.1:8081/api/auth/menus' -WebSession $session -UseBasicParsing).Content
$index = (Invoke-WebRequest -Uri 'http://127.0.0.1:8081/' -WebSession $session -UseBasicParsing).Content
$js = (Invoke-WebRequest -Uri 'http://127.0.0.1:8081/app.js?v=20260713-dashboard-init-fix' -WebSession $session -UseBasicParsing).Content

Write-Output '--- /api/auth/me (first 200 chars) ---'
Write-Output $me.Substring(0, [Math]::Min(200, $me.Length))
Write-Output '--- menu count ---'
($menus | ConvertFrom-Json).Count
Write-Output '--- index app.js version ---'
if ($index -match 'app\.js\?v=([^"]+)') { $Matches[1] } else { 'NOT FOUND' }
Write-Output '--- served js checks ---'
Write-Output ('unentered-only: ' + ($js -match 'assessment-batch-unentered-only'))
Write-Output ('result-filter optional: ' + ($js -match 'assessment-batch-result-filter"\)\?\.addEventListener'))
Write-Output ('initializeAuth present: ' + ($js -match 'async function initializeAuth'))
