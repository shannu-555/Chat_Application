#!/bin/bash

echo "========================================"
echo "Java Chat Application - Test Script"
echo "========================================"
echo
echo "This script will help you test the chat application."
echo
echo "Instructions:"
echo "1. First, start the server in a separate terminal window"
echo "2. Then start multiple clients to test the chat functionality"
echo
echo "To start the server, run: ./run-server.sh"
echo "To start a client, run: ./run-client.sh"
echo
echo "You can run multiple clients by opening multiple terminal windows"
echo "and running ./run-client.sh in each one."
echo
echo "========================================"
echo "Test Steps:"
echo "========================================"
echo "1. Open a new terminal window and run: ./run-server.sh"
echo "2. Open another terminal window and run: ./run-client.sh"
echo "3. Connect to localhost:8080 with username 'Alice'"
echo "4. Open a third terminal window and run: ./run-client.sh"
echo "5. Connect to localhost:8080 with username 'Bob'"
echo "6. Test sending messages between the clients"
echo "7. Test private messaging using @username syntax"
echo
echo "========================================"
echo "Features to Test:"
echo "========================================"
echo "- Broadcast messages (sent to all users)"
echo "- Private messages (use @username message)"
echo "- User list updates"
echo "- Connection/disconnection notifications"
echo "- Double-click users in the list for quick private messages"
echo
read -p "Press Enter to continue..."
