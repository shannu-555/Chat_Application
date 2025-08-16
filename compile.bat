@echo off
echo Compiling Java Chat Application...

REM Create bin directory if it doesn't exist
if not exist "bin" mkdir bin

REM Compile all Java files
javac -d bin -cp src src/*.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo Compiled files are in the 'bin' directory.
) else (
    echo Compilation failed!
    pause
)
