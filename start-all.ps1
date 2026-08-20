$root = $PSScriptRoot
$logDir = "$root\logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

function Start-ServiceJob($name, $delaySeconds) {
    Start-Sleep -Seconds $delaySeconds
    Write-Host "Starting $name..."
    Start-Job -Name $name -ScriptBlock {
        param($path, $log)
        Set-Location $path
        mvn spring-boot:run *> $log
    } -ArgumentList "$root\$name", "$logDir\$name.log" | Out-Null
}

Write-Host "Starting Docker containers..."
docker compose up -d
docker compose ps

Write-Host "Waiting for containers to initialize..."
Start-Sleep -Seconds 15

Start-ServiceJob "discovery-server" 0
Start-ServiceJob "config-server" 15
Start-ServiceJob "api-gateway" 15
Start-ServiceJob "user-service" 10
Start-ServiceJob "shipment-service" 10
Start-ServiceJob "tracking-service" 10

Write-Host ""
Write-Host "All services launching as background jobs in this terminal."
Write-Host "Run 'Get-Job' to see status, or use the tail commands below."