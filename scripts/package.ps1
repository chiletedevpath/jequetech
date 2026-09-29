$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$outputDirectory = Join-Path $projectRoot 'exports'
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
$sourceNames = @('.mvn', 'src', 'docs', 'scripts', 'pom.xml', 'README.md', '.gitignore', 'mvnw', 'mvnw.cmd')
$sourcePaths = $sourceNames | ForEach-Object { Join-Path $projectRoot $_ }
$archivePath = Join-Path $outputDirectory 'jequetech-apf2-fuentes.zip'
Compress-Archive -LiteralPath $sourcePaths -DestinationPath $archivePath -Force
Write-Output "Entrega de fuentes: $archivePath"
