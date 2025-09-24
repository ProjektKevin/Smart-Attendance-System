@echo off
setlocal
REM Minimal, non-recursive compile script

cd /d "%~dp0" || (echo [ERROR] Cannot cd to script folder & exit /b 1)

if exist "mvnw.cmd" (
  echo [INFO] Using Maven Wrapper
  call mvnw.cmd -q -DskipTests clean package || (echo [ERROR] Maven build failed & exit /b 1)
) else (
  echo [INFO] Using system Maven
  mvn -q -DskipTests clean package || (echo [ERROR] Maven build failed & exit /b 1)
)

echo [OK] Build finished. JARs are in target\
endlocal
