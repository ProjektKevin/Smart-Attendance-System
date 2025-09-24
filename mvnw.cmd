@echo off
REM Clean Maven Wrapper for Windows with proper multi-module property
setlocal ENABLEDELAYEDEXPANSION

REM Base dir of the project (folder of this script)
set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

REM Wrapper jar path
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

if not exist "%WRAPPER_JAR%" (
  echo [ERROR] Maven wrapper JAR not found at "%WRAPPER_JAR%"
  exit /b 1
)

REM Choose Java executable
set "JAVA_EXE=java"
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

REM Pass through all arguments; set the multiModuleProjectDirectory system property explicitly
"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %*
set "ERR=%ERRORLEVEL%"
endlocal & exit /b %ERR%
