@echo off
echo Starting Modern Chat Application Test
echo ====================================
echo.
echo 1. Starting Server...
start "Chat Server" java -cp bin Server
timeout /t 2 /nobreak >nul
echo.
echo 2. Starting Client 1...
start "Chat Client 1" java -cp bin Client
timeout /t 1 /nobreak >nul
echo.
echo 3. Starting Client 2...
start "Chat Client 2" java -cp bin Client
echo.
echo ====================================
echo Test setup complete!
echo.
echo Instructions:
echo - Connect both clients to localhost:8080
echo - Use different usernames for each client
echo - Try sending messages, emojis, and private messages
echo - Test the modern UI features
echo.
pause
