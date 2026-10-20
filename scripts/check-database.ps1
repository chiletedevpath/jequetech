# Prueba MySQL real sin puertos publicados ni acceso a bases existentes.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$imageLine = Select-String -LiteralPath (Join-Path $projectRoot 'compose.yaml') -Pattern '^\s+image:\s+(.+)$'
$image = $imageLine.Matches[0].Groups[1].Value.Trim()
$containerName = 'jequetech-schema-test-' + [guid]::NewGuid().ToString('N').Substring(0, 12)
$previousPassword = $env:MYSQL_ROOT_PASSWORD
$created = $false
$env:MYSQL_ROOT_PASSWORD = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')
$client = 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql --protocol=TCP -h127.0.0.1 --default-character-set=utf8mb4 -uroot --batch --skip-column-names jequetech'

function Invoke-Docker {
    param([string[]]$Arguments)
    $output = & docker @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Docker terminó con código $LASTEXITCODE." }
    return $output
}

function Wait-Database {
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        & docker exec $containerName sh -c ($client + ' -e "SELECT 1"') 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) { return }
        Start-Sleep -Seconds 2
    }
    throw 'MySQL no quedó disponible dentro del plazo de prueba.'
}

try {
    Invoke-Docker @('run', '-d', '--name', $containerName,
        '-e', 'MYSQL_ROOT_PASSWORD', '-e', 'MYSQL_DATABASE=jequetech', $image) | Out-Null
    $created = $true
    Wait-Database
    Invoke-Docker @('cp', (Join-Path $projectRoot 'database/schema.sql'), "${containerName}:/tmp/schema.sql") | Out-Null
    Invoke-Docker @('cp', (Join-Path $projectRoot 'database/tests/integridad.sql'), "${containerName}:/tmp/integridad.sql") | Out-Null
    Invoke-Docker @('exec', $containerName, 'sh', '-c', ($client + ' < /tmp/schema.sql'))
    Invoke-Docker @('exec', $containerName, 'sh', '-c', ($client + ' < /tmp/integridad.sql'))
    $tables = Invoke-Docker @('exec', $containerName, 'sh', '-c',
        ($client + ' -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()"'))
    if ("$tables".Trim() -ne '8') { throw 'El esquema no contiene las ocho tablas esperadas.' }

    # Dato confirmado antes de reiniciar; debe sobrevivir al arranque del servidor.
    $insert = "INSERT INTO cliente (nombres, documento, telefono, correo) VALUES ('Persistencia', 'REINICIO-001', '900000001', 'persistencia@example.test')"
    Invoke-Docker @('exec', $containerName, 'sh', '-c', ($client + ' -e "' + $insert + '"'))
    Invoke-Docker @('restart', $containerName) | Out-Null
    Wait-Database
    $retained = Invoke-Docker @('exec', $containerName, 'sh', '-c',
        ($client + ' -e "SELECT COUNT(*) FROM cliente WHERE documento=''REINICIO-001''"'))
    if ("$retained".Trim() -ne '1') { throw 'El dato no sobrevivió al reinicio de MySQL.' }
    $version = Invoke-Docker @('exec', $containerName, 'sh', '-c', ($client + ' -e "SELECT VERSION()"'))
    Write-Output "PASS: 8 tablas; persistencia tras reinicio; MySQL $version."
} finally {
    try {
        if ($created) {
            Invoke-Docker @('rm', '-f', '-v', $containerName) | Out-Null
        }
    } finally {
        $env:MYSQL_ROOT_PASSWORD = $previousPassword
    }
}
