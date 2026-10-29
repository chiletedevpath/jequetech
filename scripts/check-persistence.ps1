param([switch]$Browser)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$output = Join-Path $projectRoot 'exports/verificacion-mysql'
New-Item -ItemType Directory -Force -Path $output | Out-Null
@{ result='RUNNING'; profile='mysql' } | ConvertTo-Json | Set-Content (Join-Path $output 'resultado.json')
$image = (Select-String -LiteralPath (Join-Path $projectRoot 'compose.yaml') -Pattern '^\s+image:\s+(.+)$').Matches[0].Groups[1].Value.Trim()
$name = 'jequetech-jpa-test-' + [guid]::NewGuid().ToString('N').Substring(0, 12)
$keys = @('MYSQL_ROOT_PASSWORD','MYSQL_PASSWORD','MYSQL_USER','MYSQL_URL','JEQUETECH_MYSQL_TEST','BASE_URL','EVIDENCE_DIR')
$previous = @{}
foreach ($key in $keys) { $previous[$key] = [Environment]::GetEnvironmentVariable($key, 'Process') }
$created = $false
$runState = @{ Process = $null }
# Oracle javapath puede lanzar otro proceso. Usar el ejecutable real del JDK.
$javaSettings = (cmd /c "java -XshowSettings:properties -version 2>&1" | Out-String)
$javaHomeMatch = [regex]::Match($javaSettings, '(?m)^\s*java.home\s*=\s*(.+)$')
if (-not $javaHomeMatch.Success) { throw 'No se pudo localizar el JDK real.' }
$javaExecutable = Join-Path $javaHomeMatch.Groups[1].Value.Trim() 'bin/java.exe'
$env:MYSQL_ROOT_PASSWORD = [guid]::NewGuid().ToString('N')
$env:MYSQL_PASSWORD = [guid]::NewGuid().ToString('N')
$env:MYSQL_USER = 'jequetech'
$env:JEQUETECH_MYSQL_TEST = 'true'
$client = 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql --protocol=TCP -h127.0.0.1 -uroot --batch --skip-column-names jequetech'

function Docker-Checked {
    param([string[]]$Arguments)

    $result = & docker @Arguments

    if ($LASTEXITCODE -ne 0) {
        throw "Docker falló: $LASTEXITCODE"
    }

    return $result
}

function Wait-Database {
    for ($i = 0; $i -lt 90; $i++) {
        $previousErrorAction = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'

        try {
            & docker exec $name sh -c ($client + ' -e "SELECT 1"') 2>$null | Out-Null
            $exitCode = $LASTEXITCODE
        }
        finally {
            $ErrorActionPreference = $previousErrorAction
        }

        if ($exitCode -eq 0) {
            return
        }

        Start-Sleep -Seconds 2
    }

    throw 'MySQL no está disponible.'
}
function Start-App {
    param([string]$Phase)
    $jar = Join-Path $projectRoot 'target/jequetech-0.1.0.jar'
    $runState.Process = Start-Process -FilePath $javaExecutable -ArgumentList @('-jar', ('"' + $jar + '"'), '--spring.profiles.active=mysql', "--server.port=$appPort") -WorkingDirectory $projectRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $output "$Phase.log") -RedirectStandardError (Join-Path $output "$Phase-error.log")
    for ($i = 0; $i -lt 60; $i++) {
        $runState.Process.Refresh()
        if ($runState.Process.HasExited) { throw "La aplicación terminó: consultar $Phase.log" }
        try {
            $page = Invoke-WebRequest -Uri "$env:BASE_URL/login" -UseBasicParsing
            # Algunas versiones de NetTCPIP no interpretan State; comprobar puerto y PID directamente.
            $owner = Get-NetTCPConnection -LocalPort $appPort -ErrorAction SilentlyContinue
            if ($page.StatusCode -eq 200 -and $owner.OwningProcess -contains $runState.Process.Id) { return }
        } catch { }
        Start-Sleep -Seconds 2
    }
    throw 'La aplicación no respondió dentro del plazo.'
}
function Stop-App {
    if ($null -ne $runState.Process) {
        $runState.Process.Refresh()
        if (-not $runState.Process.HasExited) {
            Stop-Process -Id $runState.Process.Id -Force
            if (-not $runState.Process.WaitForExit(15000)) { throw 'No se detuvo la instancia de prueba.' }
        }
        $runState.Process.Dispose()
    }
    $runState.Process = $null
}
function Snapshot {
    $rows = Docker-Checked @(
        'exec',
        $name,
        'sh',
        '-c',
        'MYSQL_PWD="$MYSQL_PASSWORD" exec mysqldump -u jequetech --no-tablespaces --skip-comments --compact --no-create-info --skip-extended-insert --skip-add-locks --order-by-primary jequetech'
    )

    $bytes = [Text.Encoding]::UTF8.GetBytes(($rows -join "`n"))
    $sha256 = [Security.Cryptography.SHA256]::Create()

    try {
        $hash = $sha256.ComputeHash($bytes)
        return ([BitConverter]::ToString($hash)).Replace('-', '')
    }
    finally {
        $sha256.Dispose()
    }
}
try {
    Docker-Checked @('run', '-d', '--name', $name, '-p', '127.0.0.1::3306',
        '-e', 'MYSQL_ROOT_PASSWORD', '-e', 'MYSQL_PASSWORD', '-e', 'MYSQL_USER', '-e', 'MYSQL_DATABASE=jequetech', $image) | Out-Null
    $created = $true
    Wait-Database
    Docker-Checked @('cp', (Join-Path $projectRoot 'database/schema.sql'), "${name}:/tmp/schema.sql") | Out-Null
    Docker-Checked @('exec', $name, 'sh', '-c', ($client + ' < /tmp/schema.sql')) | Out-Null
    $port = (Docker-Checked @('port', $name, '3306/tcp')).Trim().Split(':')[-1]
    $env:MYSQL_URL = "jdbc:mysql://127.0.0.1:${port}/jequetech?allowPublicKeyRetrieval=true&sslMode=DISABLED"
    Push-Location $projectRoot
    try {
        & .\mvnw.cmd -B verify '-Dspring.profiles.active=mysql' > (Join-Path $output 'maven.log') 2>&1
        if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas: consultar exports/verificacion-mysql/maven.log' }
    } finally { Pop-Location }
    $suites = @(Get-ChildItem (Join-Path $projectRoot 'target/surefire-reports/TEST-*.xml') | ForEach-Object {
        ([xml](Get-Content -Raw -LiteralPath $_.FullName)).testsuite
    })
    $mysqlSuite = @($suites | Where-Object { $_.name -eq 'pe.jequetech.PersistenciaMySqlTest' })
    if ($mysqlSuite.Count -ne 1 -or [int]$mysqlSuite[0].tests -lt 1) { throw 'Falta el informe de pruebas MySQL.' }
    foreach ($suite in $suites) {
        if ([int]$suite.failures -ne 0 -or [int]$suite.errors -ne 0 -or [int]$suite.skipped -ne 0) {
            throw "Pruebas fallidas u omitidas: $($suite.name)"
        }
    }
    $testCount = ($suites | ForEach-Object { [int]$_.tests } | Measure-Object -Sum).Sum
    $listener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 0)
    $listener.Start(); $appPort = $listener.LocalEndpoint.Port; $listener.Stop()
    $env:BASE_URL = "http://127.0.0.1:$appPort"
    $env:EVIDENCE_DIR = Join-Path $output 'navegador'
    Start-App 'arranque'
    if ($Browser) {
        Push-Location $projectRoot
        try {
            & node scripts/check-browser.cjs
            if ($LASTEXITCODE -ne 0) { throw 'Falló el recorrido de navegador.' }
        } finally { Pop-Location }
    }
    $before = Snapshot
    Stop-App
    Docker-Checked @('restart', $name) | Out-Null
    Wait-Database
    # Docker puede reasignar el puerto publicado aleatorio al reiniciar.
    $port = (Docker-Checked @('port', $name, '3306/tcp')).Trim().Split(':')[-1]
    $env:MYSQL_URL = "jdbc:mysql://127.0.0.1:${port}/jequetech?allowPublicKeyRetrieval=true&sslMode=DISABLED"
    Start-App 'reinicio'
    $after = Snapshot
    if ($before -ne $after) { throw 'El reinicio alteró los datos o la semilla los sobrescribió.' }
    $report = [ordered]@{ result='PASS'; profile='mysql'; tests=$testCount; failures=0; errors=0; skipped=0; sameDataAfterRestart=$true; browser=[bool]$Browser }
    $report | ConvertTo-Json | Set-Content (Join-Path $output 'resultado.json')
    'PASS: JPA, transacciones, concurrencia y datos íntegros tras reiniciar aplicación y MySQL.'
} catch {
    @{ result='FAIL'; profile='mysql'; message='Consultar los registros de esta ejecución.' } | ConvertTo-Json | Set-Content (Join-Path $output 'resultado.json')
    throw
} finally {
    try {
        try { Stop-App }
        finally { if ($created) { Docker-Checked @('rm', '-f', '-v', $name) | Out-Null } }
    }
    finally { foreach ($key in $keys) { [Environment]::SetEnvironmentVariable($key, $previous[$key], 'Process') } }
}
