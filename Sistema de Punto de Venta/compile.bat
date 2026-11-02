@echo off
cd /d "%~dp0"
set OUT_DIR=out
set JDK="C:\Program Files\Java\jdk-25.0.4\bin\javac.exe"
mkdir "%OUT_DIR%" 2>nul
if exist sources_local.txt del sources_local.txt
powershell -NoProfile -Command "Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { 'src\' + $_.FullName.Substring((Resolve-Path src).Path.Length+1) } | Set-Content -Path sources_local.txt -Encoding ASCII"
%JDK% -cp "%OUT_DIR%;lib\mysql-connector-j-9.4.0.jar" -d "%OUT_DIR%" -sourcepath "src" @sources_local.txt
set RESULT=%errorlevel%
del sources_local.txt 2>nul
if %RESULT% neq 0 (
  echo ERROR DE COMPILACION
) else (
  echo COMPILACION EXITOSA
)
