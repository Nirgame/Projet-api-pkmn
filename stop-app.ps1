$ErrorActionPreference = 'Stop'

$listeners = netstat -ano | Select-String ':9012'
$stopped = $false

foreach ($listener in $listeners) {
    $parts = ($listener.ToString() -split '\s+') | Where-Object { $_ }
    $processId = $parts[-1]
    if ($processId -match '^\d+$') {
        Stop-Process -Id ([int]$processId) -Force -ErrorAction SilentlyContinue
        $stopped = $true
    }
}

if ($stopped) {
    Write-Host 'Application arretee sur le port 9012.'
} else {
    Write-Host 'Aucune application detectee sur le port 9012.'
}
