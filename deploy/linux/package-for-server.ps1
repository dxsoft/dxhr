# 在 Windows 开发机打包 Linux 安装目录
$ErrorActionPreference = "Stop"
$Root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
if (-not (Test-Path (Join-Path $Root "pom.xml"))) {
    $Root = "e:\dxhr"
}
$Out = Join-Path $Root "dist\linux-install"
Set-Location $Root
mvn -DskipTests package
if (Test-Path $Out) { Remove-Item -Recurse -Force $Out }
New-Item -ItemType Directory -Path $Out | Out-Null
Copy-Item (Join-Path $Root "target\rsgzgl-0.1.0-SNAPSHOT.jar") (Join-Path $Out "app.jar")
Copy-Item (Join-Path $Root "deploy\linux\install.sh") $Out
Copy-Item (Join-Path $Root "deploy\linux\rsgzgl.service") $Out
Copy-Item (Join-Path $Root "deploy\linux\app.env.example") $Out
if (Test-Path (Join-Path $Root "deploy\saas")) {
    Copy-Item (Join-Path $Root "deploy\saas") (Join-Path $Out "saas") -Recurse -Force
}
Write-Host "已生成: $Out"
Write-Host "单机安装: scp -r dist/linux-install user@SERVER:/tmp/rsgzgl-install && sudo bash install.sh"
Write-Host "托管 SaaS: 见 dist/linux-install/saas/README.md"
