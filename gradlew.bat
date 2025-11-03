@echo off

@if "%DEBUG%" == "" @echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set DEFAULT_JVM_OPTS=

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

for %%i in (java.exe) do set JAVA_EXE=%%~$PATH:i
if not "%JAVA_HOME%" == "" goto findJavaFromJavaHome

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:="=%
if exist "%JAVA_HOME%\bin\java.exe" goto init

for %%i in ((java.exe)) do set JAVA_EXE=%%~$PATH:i
if defined JAVA_EXE goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
location of your Java installation.

goto fail

:init
set COMMAND_LINE_ARGS=
if not "%OS%" == "Windows_NT" goto win9xME_args
setlocal enabledelayedexpansion
set CMD_LINE_ARGS=
:win9xME_args
set CMD_LINE_ARGS=%*

"%JAVA_HOME%\bin\java.exe" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %CMD_LINE_ARGS%

endlocal & set EXIT_CODE=%ERRORLEVEL%
if not "%EXIT_CODE%" == "0" goto fail

:mainEnd
if "%OS%" == "Windows_NT" endlocal

:end
exit /b 0

:fail
if "%OS%" == "Windows_NT" endlocal
exit /b %EXIT_CODE%
