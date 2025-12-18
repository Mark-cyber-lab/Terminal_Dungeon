#!/bin/bash
set -e

# Ensure zip is available
if ! command -v zip >/dev/null; then
  echo "zip not found. Installing..."

  case "$OSTYPE" in
    linux-gnu*)
      if command -v apt-get >/dev/null; then
        sudo apt-get update
        sudo apt-get install -y zip
      else
        echo "Error: unsupported Linux package manager."
        exit 1
      fi
      ;;
    darwin*)
      if command -v brew >/dev/null; then
        brew install zip
      else
        echo "Error: Homebrew is required to install zip."
        exit 1
      fi
      ;;
    *)
      echo "Error: automatic zip installation not supported on this OS."
      exit 1
      ;;
  esac
fi

# Variables
DIST_DIR=dist
APP_DIR=app
JAR_NAME=terminaldungeon-1.0.jar
ZIP_NAME=Terminal_Dungeon.zip

echo "=== Terminal Dungeon Export ==="

# Build
./gradlew build

# Prepare directories
rm -rf "$DIST_DIR" "$APP_DIR"
mkdir -p "$DIST_DIR" "$APP_DIR"

# Stage files
cp "build/output/$JAR_NAME" "$DIST_DIR/"
cp install.sh game-start "$DIST_DIR/"
chmod +x "$DIST_DIR/"{install.sh,game-start}

# Zip only dist contents into app/
(
  cd "$DIST_DIR"
  zip -r "../$APP_DIR/$ZIP_NAME" .
)

echo "Package created: $APP_DIR/$ZIP_NAME"
