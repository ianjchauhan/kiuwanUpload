@echo off
echo.
echo #       #
echo #       #
echo #
echo #    #  #   #    #  #          #   ###    # ###
echo #   #   #   #    #   #   ##   ##  #   #   ###  #
echo #  #    #   #    #   #   ##   #       #   #    #
echo ####    #   #    #   #  ## #  #    ####   #    #
echo #  #    #   #    #    # #  # #    #   #   #    #
echo #  ##   #   #    #    # #  # #   #    #   #    #
echo #   #   #   #    #    ##    ##   #    #   #    #
echo #    #  #   ######     #    #     #####   #    #
echo.
echo                                    www.kiuwan.com
echo.                        
:alpha

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem ---------------------------------------------- setDefaultHome
:setDefaultHome
rem %~dp0 is expanded pathname of the current script under NT
set AGENT_HOME=%~dp0..
set BASE_DIR=%AGENT_HOME%\bin
set _CP_=%AGENT_HOME%\lib\*;%AGENT_HOME%\conf\;%AGENT_HOME%\lib.custom\*
set _JAVA_OPTS=-DJKQA_HOME="%AGENT_HOME%" -DAGENT_HOME="%AGENT_HOME%" -DTASK.LOG="%AGENT_HOME%/temp/agent.log" -showversion
if NOT ""%1""=="""" set _PARAMS=%*
if not exist "%AGENT_HOME%\lib\KiuwanLocalAnalyzer.jar" goto noAgentHome

rem ---------------------------------------------- checkJava
:checkJava
set _JAVACMD=%JAVACMD%
if not defined JAVA_HOME goto noJavaHome
set _JAVA_HOME=%JAVA_HOME:"=%
if not exist "%_JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%_JAVA_HOME%\bin\java.exe

rem ---------------------------------------------- run
:run
if NOT ""%1""==""--clean"" (
  call :upgrade
  if errorlevel 1 (
    if "%1"=="allowpause" (
      pause
    )
  )
)
echo Launching...
echo.
"%_JAVACMD%" -classpath "%_CP_%" %_JAVA_OPTS% com.optimyth.qaking.agent.analyzer.ConsoleLauncher %_PARAMS%
set javaErrorLevel=%ERRORLEVEL%
set showError=0
if %javaErrorLevel% NEQ 0 if %javaErrorLevel% LSS 10 set showError=1 
if %showError% NEQ 1 if %javaErrorLevel% GTR 50 set showError=1
if %showError% EQU 1 (
  echo.
  echo Check the Kiuwan Local Analyzer troubleshooting guide for instructions on fixing this problem:
  echo https://www.kiuwan.com/docs/display/K5/Troubleshooting
  echo.
  if "%1"=="allowpause" (
    pause
  )
)
exit /b %javaErrorLevel%

rem --------------------------------------------- upgrade
:upgrade
"%_JAVACMD%" "-DAGENT_HOME=%AGENT_HOME%" "-DJAVACMD=%_JAVACMD%" %_JAVA_OPTS% -jar "%AGENT_HOME%\lib\upgrade.jar"
              
if errorlevel 1 (
  echo.
  echo Error: Local analyzer upgrade failed. Check Internet connection from your computer.
  echo If under a web proxy, check proxy configuration.
  echo.
  echo Alternatively, you may perform a manual analyzer upgrade if necessary,
  echo following the instructions in kiuwan website.
  echo.
  echo Check the Kiuwan Local Analyzer troubleshooting guide for instructions on fixing this problem:
  echo https://www.kiuwan.com/docs/display/K5/Troubleshooting
  echo.
  exit /b %ERRORLEVEL%
)
if exist "%AGENT_HOME%\lib\upgrade.jar.new" (
  move /y "%AGENT_HOME%\lib\upgrade.jar.new" "%AGENT_HOME%\lib\upgrade.jar" > nul
)
exit /b

rem ------------------------------------------------------- noAgentHome
:noAgentHome
echo Error: cannot execute Kiuwan Local Analyzer: expected main file not found. Installation may be corrupt, reinstall Kiuwan Local Analyzer and try again.  
echo AGENT_HOME is: %AGENT_HOME%
exit /b 1

rem ------------------------------------------------------- noJavaHome
:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

rem try to check if java is installed
for /f %%j in ("java.exe") do (
  set _JAVA_PATH=%%~dp$PATH:j
)

if "%_JAVA_PATH%" == "" (
  @echo Java executable not found. Kiuwan Local Analyzer needs JDK 1.8 or higher to run.
  @echo.
  @echo You may download Java from http://www.oracle.com/technetwork/java/javase/downloads
  @echo.
  exit /b 1
)
goto run

rem ------------------------------------------------------- end
:end

if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal

:omega
