@echo off
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

:setDefaultHome
set AGENT_HOME=%~dp0..
set BASE_DIR=%AGENT_HOME%\bin
set _CP_=%AGENT_HOME%\lib\ant-launcher.jar;%AGENT_HOME%\lib\ant.jar
set _JAVA_OPTS=-DJKQA_HOME="%AGENT_HOME%" -DAGENT_HOME="%AGENT_HOME%" -Dant.home="%AGENT_HOME%" -DTASK.LOG="%AGENT_HOME%/temp/agent.log"
if NOT ""%1""=="""" set _PARAMS=%*
if not exist "%AGENT_HOME%\lib\KiuwanLocalAnalyzer.jar" goto noAgentHome

:checkJava
set _JAVACMD=%JAVACMD%
if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe

:run
"%_JAVACMD%" %ANT_OPTS% -cp "%_CP_%" %_JAVA_OPTS% org.apache.tools.ant.launch.Launcher %_PARAMS%
set javaErrorLevel=%ERRORLEVEL%
if errorlevel 1 (
  echo.
  echo Error executing runant.cmd script
  echo.
)
exit /b %javaErrorLevel%

:noAgentHome
echo Error: you are executing the script into an unknown location or the installation is corrupt. Please run it under unzipped_agent/bin folder or reinstall.
echo AGENT_HOME is: %AGENT_HOME%
goto end

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

rem try to check if java is installed
for /f %%j in ("java.exe") do (
  set _JAVA_PATH=%%~dp$PATH:j
)

if %_JAVA_PATH%.==. (
  @echo Java executable not found. runant needs JDK 1.8 or higher to run.
  @echo.
  @echo You may download Java from http://www.oracle.com/technetwork/java/javase/downloads
  @echo.
  goto end
)
goto run

:end
rem bug ID 32069: resetting an undefined env variable changes the errorlevel.
if not "%_JAVACMD%"=="" set _JAVACMD=

rem Set the return code if we are not in NT.  We can only set
rem a value of 1, but it's better than nothing.
if not "%OS%"=="Windows_NT" echo 1 > nul | choice /n /c:1

rem Set the ERRORLEVEL if we are running NT.
if "%OS%"=="Windows_NT" color 00

goto omega

:mainEnd

if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal

:omega
