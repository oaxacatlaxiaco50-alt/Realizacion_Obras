$env:PGPASSWORD = "12345678"
$pgDump = "C:\Program Files\PostgreSQL\18\bin\pg_dump.exe"

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host " Exportando Datos de PostgreSQL Local (obras_db)..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

if (Test-Path $pgDump) {
    & $pgDump -h localhost -U postgres -d obras_db --data-only --column-inserts --disable-triggers --file=seed_data_export.sql
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n[EXITO] Datos exportados correctamente a 'seed_data_export.sql'." -ForegroundColor Green
        Write-Host "Puedes enviar este archivo a tus compañeros para compartir los avances de la base de datos.`n" -ForegroundColor Yellow
    } else {
        Write-Host "`n[ERROR] Fallo la exportacion de datos.`n" -ForegroundColor Red
    }
} else {
    Write-Host "[ERROR] No se encontro pg_dump.exe en $pgDump" -ForegroundColor Red
}
