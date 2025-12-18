#!/bin/bash
set -e  # Exit on any error

# Directories
SRC_DIR="src"
OUT_DIR="out"
RES_DIR="$SRC_DIR/resources"

# Clean previous build
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

# Compile all Java files
echo "Compiling Java files..."
javac -d "$OUT_DIR" $(find "$SRC_DIR" -name "*.java")

# Copy resources
if [ -d "$RES_DIR" ]; then
    echo "Copying resources..."
    cp -r "$RES_DIR/"* "$OUT_DIR/"
fi

# Run the program
echo "Running program..."
java -cp "$OUT_DIR" Main
