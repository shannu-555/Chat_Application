@echo off
echo Starting Java Chat Server...

REM Check if bin directory exists
if not exist "bin" (
    echo Error: Compiled files not found. Please run compile.bat first.
    pause
    exit /b 1
)

REM Check if Server.class exists
if not exist "bin\Server.class" (
    echo Error: Server.class not found. Please run compile.bat first.
    pause
    exit /b 1
)

echo Server is starting on port 8080...
echo Press Ctrl+C to stop the server.
echo.

java -cp bin Server %*

pause
