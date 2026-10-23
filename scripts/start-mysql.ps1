$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot '.env'
$keys = @('MYSQL_PASSWORD', 'MYSQL_PORT', 'MYSQL_USER', 'MYSQL_URL', 'JEQUETECH_DEMO')
$previous = @{}
foreach ($key in $keys) { $previous[$key] = [Environment]::GetEnvironmentVariable($key, 'Process') }
try {
    if (Test-Path -LiteralPath $envFile) {
        foreach ($line in Get-Content -LiteralPath $envFile) {
            if ($line -match '^\s*([A-Z_]+)=(.*)$' -and $Matches[1] -in $keys) {
                $key = $Matches[1]; $value = $Matches[2].Trim()
                # Formato KEY=valor literal; no se evalúa código ni se registran secretos.
                if (-not [Environment]::GetEnvironmentVariable($key, 'Process')) {
                    [Environment]::SetEnvironmentVariable($key, $value, 'Process')
                }
            }
        }
    }
    if (-not $env:MYSQL_PASSWORD) { throw 'Completa MYSQL_PASSWORD en .env o en el entorno.' }
    if (-not $env:MYSQL_URL) {
        $dbPort = if ($env:MYSQL_PORT) { $env:MYSQL_PORT } else { '3307' }
        if ($dbPort -notmatch '^\d{1,5}$' -or [int]$dbPort -gt 65535 -or [int]$dbPort -lt 1) { throw 'MYSQL_PORT inválido.' }
        $env:MYSQL_URL = "jdbc:mysql://127.0.0.1:${dbPort}/jequetech?allowPublicKeyRetrieval=true&sslMode=DISABLED"
    }
    Push-Location $projectRoot
    try {
        & .\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=mysql'
        if ($LASTEXITCODE -ne 0) { throw 'La aplicación no pudo iniciarse.' }
    } finally { Pop-Location }
} finally {
    foreach ($key in $keys) { [Environment]::SetEnvironmentVariable($key, $previous[$key], 'Process') }
}
