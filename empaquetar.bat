@echo off
setlocal
cd /d "%~dp0"
set "JAVAC_CMD=javac"
set "JAR_CMD=jar"
if defined JAVA_HOME (
  set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
  set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
)
set "PAQUETE_DIR=build\paquete-%RANDOM%-%RANDOM%"
if exist "%PAQUETE_DIR%" exit /b 1
mkdir "%PAQUETE_DIR%"
if not exist dist mkdir dist
"%JAVAC_CMD%" --release 17 -encoding UTF-8 -d "%PAQUETE_DIR%" @sources.txt
if errorlevel 1 exit /b 1
"%JAR_CMD%" --create --file dist/SupermercadoEnCaos.jar --main-class com.supermercado.Main -C "%PAQUETE_DIR%" .
if errorlevel 1 exit /b 1
echo JAR generado desde las fuentes de produccion.
endlocal
