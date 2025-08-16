#!/bin/bash

echo "Compiling Java Chat Application..."

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile all Java files
javac -d bin -cp src src/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Compiled files are in the 'bin' directory."
else
    echo "Compilation failed!"
    exit 1
fi
