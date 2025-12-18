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

# build if its not built
if [ ! -f "./build/libs/Terminal_Dungeon.jar" ]; then
    echo "Building project..."
    ./gradlew build || { echo "Build failed. Exiting."; exit 1; }
fi

# Run the application
java -jar ./build/output/terminaldungeon-1.0.jar
