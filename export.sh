#!/bin/bash
set -e

# Check if zip is installed
if ! command -v zip &> /dev/null; then
    echo "zip is not installed. Installing..."
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y zip
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        if command -v brew &> /dev/null; then
            brew install zip
        fi
    fi
fi

# Variables
DIST_DIR="./dist"
JAR_NAME="terminaldungeon-1.0.jar"
BUILD_JAR="./build/output/$JAR_NAME"
OUTPUT_JAR="$DIST_DIR/$JAR_NAME"
INSTALL_SCRIPT="install.sh"
LAUNCHER_SCRIPT="game-start"
ZIP_NAME="Terminal_Dungeon.zip"

echo "=== Terminal Dungeon Export Script ==="

echo "Building project..."
./gradlew build || { echo "Build failed!"; exit 1; }


# Clean and recreate dist folder
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

# Copy JAR
cp "$BUILD_JAR" "$OUTPUT_JAR"
echo "Copied JAR to $OUTPUT_JAR"

# Copy install and launcher scripts
cp "$INSTALL_SCRIPT" "$DIST_DIR/"
cp "$LAUNCHER_SCRIPT" "$DIST_DIR/"
chmod +x "$DIST_DIR/$INSTALL_SCRIPT" "$DIST_DIR/$LAUNCHER_SCRIPT"
echo "Copied install scripts"

# Create zip archive
rm -f "$ZIP_NAME"
zip -r "$ZIP_NAME" "$DIST_DIR"
echo "Created zip package: $ZIP_NAME"

echo "Export complete! You can distribute $ZIP_NAME"
