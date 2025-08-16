#!/bin/bash

echo "Starting Java Chat Server..."

# Check if bin directory exists
if [ ! -d "bin" ]; then
    echo "Error: Compiled files not found. Please run compile.sh first."
    exit 1
fi

# Check if Server.class exists
if [ ! -f "bin/Server.class" ]; then
    echo "Error: Server.class not found. Please run compile.sh first."
    exit 1
fi

echo "Server is starting on port 8080..."
echo "Press Ctrl+C to stop the server."
echo

java -cp bin Server "$@"
