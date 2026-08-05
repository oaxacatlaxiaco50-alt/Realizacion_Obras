@echo off
echo ========================================================
echo Importando Datos a PostgreSQL Local (obras_db)...
echo ========================================================
if not exist seed_data_export.sql (
    echo [ERROR] No se encontro el archivo 'seed_data_export.sql'.
    pause
    exit /b 1
)
set PGPASSWORD=12345678
"C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -U postgres -d obras_db -f seed_data_export.sql
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [EXITO] Datos importados correctamente a obras_db.
) else (
    echo.
    echo [ERROR] Hubo un error al importar los datos.
)
pause
