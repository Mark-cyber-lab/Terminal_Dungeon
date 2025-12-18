#!/bin/bash
set -e

GAME_NAME="terminal-dungeon"
INSTALL_BIN="$HOME/.local/bin"
INSTALL_DIR="$HOME/.local/share/terminal-dungeon"

echo "Installing Terminal Dungeon..."

# Ensure directories exist
mkdir -p "$INSTALL_BIN"
mkdir -p "$INSTALL_DIR"

# Ensure ~/.local/bin is in PATH
SHELL_CONFIG="$HOME/.bashrc"
if [[ "$SHELL" == *"zsh"* ]]; then
    SHELL_CONFIG="$HOME/.zshrc"
fi

if ! echo "$PATH" | grep -q "$INSTALL_BIN"; then
    echo "export PATH=\"\$HOME/.local/bin:\$PATH\"" >> "$SHELL_CONFIG"
    echo "Added ~/.local/bin to PATH"
fi

# Install optional dependency (Ubuntu/Debian)
if command -v apt-get &> /dev/null; then
    echo "Checking dependencies..."
    sudo apt-get update
    sudo apt-get install -y tree
fi

# Copy game files
cp terminaldungeon-1.0.jar "$INSTALL_DIR/"
cp -r resources "$INSTALL_DIR/" 2>/dev/null || true

# Install game launcher
chmod +x game-start
cp game-start "$INSTALL_BIN/$GAME_NAME"

# Install tdungeon wrapper
cat > "$INSTALL_BIN/tdungeon" <<'EOF'
#!/bin/bash
set -e
case "$1" in
    start)
        terminal-dungeon
        ;;
    *)
        echo "Usage: tdungeon start"
        exit 1
        ;;
esac
EOF
chmod +x "$INSTALL_BIN/tdungeon"

echo ""
echo "Installation complete!"
echo "Reload your shell:"
echo "  source $SHELL_CONFIG"
echo ""
echo "Start the game with:"
echo "  tdungeon start"
