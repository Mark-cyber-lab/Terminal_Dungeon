#!/bin/bash
set -e

GAME_NAME="terminal-dungeon"
INSTALL_BIN="$HOME/.local/bin"
INSTALL_DIR="$HOME/.local/share/terminal-dungeon"
COMPLETION_DIR="$HOME/.local/share/bash-completion/completions"

echo "Installing Terminal Dungeon..."

# Ensure directories exist
mkdir -p "$INSTALL_BIN" "$INSTALL_DIR" "$COMPLETION_DIR"

# Ensure ~/.local/bin is in PATH
SHELL_CONFIG="$HOME/.bashrc"
if [[ "$SHELL" == *"zsh"* ]]; then
    SHELL_CONFIG="$HOME/.zshrc"
fi

if ! echo "$PATH" | grep -q "$INSTALL_BIN"; then
    echo "export PATH=\"\$HOME/.local/bin:\$PATH\"" >> "$SHELL_CONFIG"
    echo "Added ~/.local/bin to PATH"
fi

# Optional dependency (Ubuntu/Debian)
if command -v apt-get &> /dev/null; then
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

GAME_DIR="$HOME/.local/share/terminal-dungeon"
CACHE_DIR="$GAME_DIR/cache"
LOG_DIR="$GAME_DIR/logs"

FORCE=false
DRY_RUN=false
VERBOSE=false
QUIET=false
ACTION=""

log() {
    $QUIET && return
    echo "$@"
}

vlog() {
    $VERBOSE && log "$@"
}

run() {
    if $DRY_RUN; then
        log "[dry-run] $*"
    else
        "$@"
    fi
}

usage() {
cat <<USAGE
Terminal Dungeon commands

Usage:
  terminal-dungeon
  tdungeon start
  tdungeon reset [--force|-y] [--dry-run]
  tdungeon clear [--force|-y] [--dry-run]
  tdungeon help

Options:
  -y, --force     Skip confirmation prompts
  --dry-run       Show what would be deleted without deleting
  --quiet         Suppress output
  --verbose       Verbose output
USAGE
}

confirm() {
    $FORCE && return 0
    read -r -p "This action cannot be undone. Type 'yes' to continue: " reply
    [[ "$reply" == "yes" ]]
}

# Parse arguments
for arg in "$@"; do
    case "$arg" in
        start|reset|clear|help)
            ACTION="$arg"
            ;;
        -y|--force)
            FORCE=true
            ;;
        --dry-run)
            DRY_RUN=true
            ;;
        --quiet)
            QUIET=true
            ;;
        --verbose)
            VERBOSE=true
            ;;
    esac
done

case "$ACTION" in
    start)
        terminal-dungeon
        ;;
    reset)
        log "This will delete all files inside the cache directory."
        if confirm; then
            mkdir -p "$CACHE_DIR"
            vlog "Clearing cache contents"
            run rm -rf "$CACHE_DIR"/*
            log "Cache reset complete."
        else
            log "Operation cancelled."
        fi
        ;;
    clear)
        log "This will delete the cache and logs directories."
        if confirm; then
            vlog "Removing cache and logs directories"
            run rm -rf "$CACHE_DIR" "$LOG_DIR"
            log "Cache and logs cleared."
        else
            log "Operation cancelled."
        fi
        ;;
    help|"")
        usage
        ;;
    *)
        usage
        exit 1
        ;;
esac
EOF

chmod +x "$INSTALL_BIN/tdungeon"

# Bash completion
cat > "$COMPLETION_DIR/tdungeon" <<'EOF'
_tdungeon() {
    local cmds="start reset clear help"
    local opts="--force -y --dry-run --quiet --verbose"
    COMPREPLY=( $(compgen -W "$cmds $opts" -- "${COMP_WORDS[COMP_CWORD]}") )
}
complete -F _tdungeon tdungeon
EOF

# Zsh completion hook
if [[ "$SHELL" == *"zsh"* ]]; then
    echo "autoload -Uz compinit && compinit" >> "$SHELL_CONFIG"
    echo "source $COMPLETION_DIR/tdungeon" >> "$SHELL_CONFIG"
fi

echo ""
echo "Installation complete!"
echo "Reload your shell:"
echo "  source $SHELL_CONFIG"
echo ""
echo "Available commands:"
echo "  terminal-dungeon"
echo "  tdungeon start"
echo "  tdungeon reset [--force|-y] [--dry-run]"
echo "  tdungeon clear [--force|-y] [--dry-run]"
echo "  tdungeon help"
