#!/bin/bash

echo "Starting Java Chat Client..."

# Check if bin directory exists
if [ ! -d "bin" ]; then
    echo "Error: Compiled files not found. Please run compile.sh first."
    exit 1
fi

# Check if Client.class exists
if [ ! -f "bin/Client.class" ]; then
    echo "Error: Client.class not found. Please run compile.sh first."
    exit 1
fi

echo "Starting client application..."
echo "Make sure the server is running before connecting."
echo

java -cp bin Client
