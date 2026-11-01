@echo off
cd /d "%~dp0\Sistema de Punto de Venta"

echo ============================================
echo  COMPILAR Y EJECUTAR CON MYSQL
echo ============================================

:: 1. Compilar
echo [1/3] Compilando proyecto...
call compile.bat
if errorlevel 1 (
    echo ERROR DE COMPILACION
    pause
    exit /b 1
)

:: 2. Configurar variables de entorno
echo [2/3] Configurando credenciales MySQL...
set DB_SODA_URL=jdbc:mysql://localhost:3306/soda_pos?serverTimezone=UTC
set DB_SODA_USER=root
set DB_SODA_PASSWORD=rootroot

:: 3. Ejecutar
echo [3/3] Ejecutando aplicación...
java -cp "out;lib\mysql-connector-j-9.4.0.jar" main.Main
pause