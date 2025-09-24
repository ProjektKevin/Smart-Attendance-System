@echo off
setlocal
REM Run with .env support

cd /d "%~dp0" || (echo [ERROR] Cannot cd to script folder & exit /b 1)

REM Load .env if present (ignores lines starting with #)
if exist ".env" (
  echo [INFO] Loading .env
  for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do (
    if not "%%~A"=="" set "%%~A=%%~B"
  )
)

REM Try Maven JavaFX first
if exist "mvnw.cmd" (
  echo [INFO] mvnw javafx:run
  call mvnw.cmd -q -DskipTests javafx:run %* && (echo [OK] App exited. & exit /b 0)
) else (
  echo [INFO] mvn javafx:run
  mvn -q -DskipTests javafx:run %* && (echo [OK] App exited. & exit /b 0)
)

REM Fallback to newest JAR
for /f "delims=" %%J in ('dir /b /o:-d "target\*.jar" 2^>nul') do (
  set "APPJAR=target\%%J"
  goto :runjar
)

echo [ERROR] Could not run via Maven and no JAR found in target\
exit /b 1

:runjar
echo [INFO] java -jar "%APPJAR%"
java -jar "%APPJAR%"
endlocal
