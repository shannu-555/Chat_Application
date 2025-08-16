@echo off
echo Starting Java Chat Client...

REM Check if bin directory exists
if not exist "bin" (
    echo Error: Compiled files not found. Please run compile.bat first.
    pause
    exit /b 1
)

REM Check if Client.class exists
if not exist "bin\Client.class" (
    echo Error: Client.class not found. Please run compile.bat first.
    pause
    exit /b 1
)

echo Starting client application...
echo Make sure the server is running before connecting.
echo.

java -cp bin Client

pause
