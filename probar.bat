@echo off
setlocal
cd /d "%~dp0"
set "JAVA_CMD=java"
set "JAVAC_CMD=javac"
if defined JAVA_HOME (
  set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
  set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
)
if not exist "build\classes" mkdir "build\classes"
"%JAVAC_CMD%" --release 17 -encoding UTF-8 -d build/classes @sources.txt @tests.txt
if errorlevel 1 (
  echo.
  echo No se pudo compilar. Configura un JDK 17 o superior en JAVA_HOME o PATH.
  pause
  exit /b 1
)
"%JAVA_CMD%" -cp build/classes com.supermercado.PruebasJuego
if errorlevel 1 (
  echo.
  echo Revisa el error anterior. Se necesita Java 17 o superior.
  pause
  exit /b 1
)
pause
endlocal
