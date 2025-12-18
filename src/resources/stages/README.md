# Config File Guide

`DirGenerator` is a utility that generates directories and files based on a plain text configuration file. This guide
explains the formatting rules and examples for creating config files.
---
## 1. File Structure

- Each line represents a directory, file, or special directive.
- Lines can include comments starting with `#`.
- Blank lines are ignored.
- Files with multi-line content are enclosed between a `FILE`: (or `HIDDEN_FILE:`) line and an `END` or `END_FILE` line.
---
## 2. Supported Entries

### 2.1 Directories

- Normal directory:

    ```txt
    DIR: path/to/directory
    ```
  or just:

    ```txt
    path/to/directory/
    ```

- Hidden directory (starts with a dot `.` or uses `HIDDEN_DIR:`):

    ```txt
    HIDDEN_DIR: path/to/.hiddenDirectory
    ```
  or simply:
    ```txt
    .hiddenDirectory/
    ```

### 2.2 Files

- Normal file:

    ```txt
    FILE: path/to/file.txt
    ```

- Multi-line content example:

    ```txt
    FILE: notes.txt
    Line 1
    Line 2
    END
    ```
- Hidden file:

    ```txt
    HIDDEN_FILE: .env
    API_KEY=12345
    SECRET_KEY=abcdef
    END
    ```

- Single-line standalone file (content optional):

```txt
config.json
```

Hidden files can also be automatically determined based on extensions, e.g., `.env`, `.gitignore`. You can configure
these
in `GenerationConfig`.

### 2.3 End Markers

- To finish a multi-line file content:

    ```txt
    END
    ```

    or
    
    ```txt
    END_FILE
    ```

Standalone directories or files do not require `END`.

### 2.4 Comments

Lines starting with # are ignored.

Example:

```txt
# This is a comment
DIR: src/
```
---
## 3. Multi-Line File Content

- Begin with `FILE:` or `HIDDEN_FILE:` followed by the path.
- Add file content line by line.
- End with `END` or `END_FILE`.

Example:

    ```txt
    FILE: README.md
    # Project README
    This project generates directories and files based on config.
    END
    ```
---
## 4. Hidden Files and Directories

- Use `HIDDEN_FILE:` or `HIDDEN_DIR:` explicitly.
- Or, a file/directory starting with `.` will automatically be hidden.

Configurable hidden extensions in `GenerationConfig`:

```java
Set<String> hiddenExts = Set.of(".env", ".gitignore", ".npmignore");
```
---
## 5. Special Notes

Paths are relative to the sandbox path provided in `GenerationConfig`.

Overwriting existing files can be enabled with:

```java
GenerationConfig.builder().overwriteExisting(true).build();
```

Hidden file behavior differs by OS:
- Windows → uses file attributes (`+H`).
- Linux/macOS → prepends a dot `.` to file name.
---
## 6. Full Example Config

```txt
# Hidden directory example

HIDDEN_DIR: .config/

# Normal directories

DIR: src/
DIR: src/main/

# Hidden file example

HIDDEN_FILE: .env
API_KEY=12345
SECRET_KEY=abcdef
END

# Normal file example

FILE: README.md

# Project README

This project demonstrates directory and file generation.
END

# Standalone directory

logs/

# Standalone file

config.json
```

---
## 7. Tips

- Always use `END` for multi-line files.
- Avoid mixing tabs and spaces; use consistent indentation.
- Comments and blank lines improve readability but are ignored by the generator.