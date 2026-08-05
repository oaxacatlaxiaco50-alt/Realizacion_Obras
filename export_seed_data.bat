@echo off
echo ========================================================
echo Exportando Datos de PostgreSQL Local (obras_db)...
echo ========================================================
set PGPASSWORD=12345678
"C:\Program Files\PostgreSQL\18\bin\pg_dump.exe" -h localhost -U postgres -d obras_db --data-only --column-inserts --disable-triggers --file=seed_data_export.sql
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [EXITO] Los datos se han exportado correctamente a 'seed_data_export.sql'.
    echo Puedes compartir este archivo con tu equipo para que importen los avances.
) else (
    echo.
    echo [ERROR] Hubo un error al exportar la base de datos.
)
pause
