#!/bin/bash

# Detect Linux + Ubuntu
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    if command -v apt-get &> /dev/null; then
        echo "Ubuntu/Debian detected."

        # Check if binary exists
        if ! command -v tree &> /dev/null; then
            echo "tree is not installed. Installing..."
            sudo apt-get update
            sudo apt-get install -y tree
        else
            echo "tree already installed."
        fi
    fi
fi

# Compile and run Java
javac -d out $(find src -name "*.java") &&
java -cp out Main
