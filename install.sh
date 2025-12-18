#!/bin/bash
set -e

GAME_NAME="terminal-dungeon"
INSTALL_BIN="$HOME/.local/bin"
SCRIPT_NAME="game-start"
SHELL_CONFIG=""

echo "Installing Terminal Dungeon..."

# Detect shell
if [[ "$SHELL" == *"zsh"* ]]; then
    SHELL_CONFIG="$HOME/.zshrc"
else
    SHELL_CONFIG="$HOME/.bashrc"
fi

# Ensure local bin exists
mkdir -p "$INSTALL_BIN"

# Ensure ~/.local/bin is in PATH
if ! echo "$PATH" | grep -q "$INSTALL_BIN"; then
    echo "export PATH=\"\$HOME/.local/bin:\$PATH\"" >> "$SHELL_CONFIG"
    echo "Added ~/.local/bin to PATH"
fi

# Install required packages (Ubuntu/Debian)
if [[ "$OSTYPE" == "linux-gnu"* ]] && command -v apt-get &> /dev/null; then
    echo "Checking dependencies..."
    sudo apt-get update
    sudo apt-get install -y tree
fi

# Make launcher executable
chmod +x "$SCRIPT_NAME"

# Copy launcher to local bin
cp "$SCRIPT_NAME" "$INSTALL_BIN/$GAME_NAME"

# Add `game start` function if missing
if ! grep -q "game()" "$SHELL_CONFIG"; then
cat << 'EOF' >> "$SHELL_CONFIG"

# Terminal Dungeon CLI
game() {
    if [ "$1" = "start" ]; then
        terminal-dungeon
    else
        echo "Usage: game start"
    fi
}
EOF
    echo "Added 'game start' command"
fi

echo ""
echo "Installation complete!"
echo "Restart your terminal or run:"
echo "  source $SHELL_CONFIG"
echo ""
echo "Start the game with:"
echo "  game start"
