$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$javaHome = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { $null }
$mavenCmd = $null
$javaCmd = $null
$repoLocal = '.mvn-local-repo'
$stdoutLog = 'target/app-stdout.log'
$stderrLog = 'target/app-stderr.log'

if ($javaHome -and (Test-Path $javaHome)) {
    $javaCmd = Join-Path $javaHome 'bin\java.exe'
}

if (-not $javaCmd) {
    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($javaCommand) {
        $javaCmd = $javaCommand.Source
        $javaHome = Split-Path -Parent (Split-Path -Parent $javaCmd)
    }
}

if (-not $javaCmd -or -not (Test-Path $javaCmd)) {
    throw 'JDK introuvable. Definis JAVA_HOME ou ajoute java.exe au PATH.'
}

$mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if (-not $mavenCommand) {
    $mavenCommand = Get-Command mvn -ErrorAction SilentlyContinue
}

if ($mavenCommand) {
    $mavenCmd = $mavenCommand.Source
}

if (-not $mavenCmd -or -not (Test-Path $mavenCmd)) {
    throw 'Maven introuvable. Ajoute mvn.cmd au PATH.'
}

$listeners = netstat -ano | Select-String ':9012'
foreach ($listener in $listeners) {
    $parts = ($listener.ToString() -split '\s+') | Where-Object { $_ }
    $processId = $parts[-1]
    if ($processId -match '^\d+$') {
        Stop-Process -Id ([int]$processId) -Force -ErrorAction SilentlyContinue
    }
}

Start-Sleep -Seconds 2

$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"

& $mavenCmd "-Dmaven.repo.local=$repoLocal" -q package

$jarPath = (Resolve-Path 'target\pokemon-tcg-collection-2.0.0.jar').Path
$javaArgs = "-jar `"$jarPath`""

if (Test-Path $stdoutLog) {
    Remove-Item $stdoutLog -Force
}

if (Test-Path $stderrLog) {
    Remove-Item $stderrLog -Force
}

$process = Start-Process -FilePath $javaCmd `
    -ArgumentList $javaArgs `
    -WorkingDirectory $projectRoot `
    -RedirectStandardOutput $stdoutLog `
    -RedirectStandardError $stderrLog `
    -PassThru

Write-Host "Application demarree. PID=$($process.Id)"
Write-Host 'URL: http://localhost:9012'
Write-Host "Logs: $stdoutLog / $stderrLog"
