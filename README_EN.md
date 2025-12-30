**English** | [**中文**](README.md)

# Galgame Search Tool

A desktop tool for searching Galgame resources. This project provides two versions: **Python Version** (Original) and **Java Version** (Refactored).

## 📁 Project Structure

- `python/`: Source code for the Python version
- `java/`: Source code for the Java version
- `galsearch-py.exe`: Pre-compiled executable for the Python version
- `galsearch-java.exe`: Pre-compiled executable for the Java version

---

## 🐍 Python Version

### Principles
Developed based on Python 3 and PySide6 (Qt for Python).
- **UI**: Modern GUI built with PySide6.
- **Crawler**: Uses `requests` library to query resource sites (like Nyaa) and parses HTML to extract magnet links.
- **Features**: Supports keyword search, result listing, and magnet link copying/downloading.

### Usage
Directly double-click to run **`galsearch-py.exe`** in the root directory.

### Build Instructions (Development)
If you need to modify the code and repackage:
1. Enter the `python` directory:
   ```powershell
   cd python
   ```
2. Install dependencies:
   ```powershell
   pip install -r requirements.txt
   ```
3. Package using PyInstaller:
   ```powershell
   pyinstaller --onefile --noconsole --name galsearch-py --icon=ico.ico --add-data "icon.png;." --clean --distpath .. main.py
   ```
   *(Note: The packaged exe will be output to the parent directory, or move it manually from `dist`)*

---

## ☕ Java Version

### Principles
Developed based on Java 21 and JavaFX.
- **UI**: Borderless window interface built with JavaFX, styled with CSS.
- **Build**: Uses Maven for dependency management, `maven-shade-plugin` for packaging dependencies, and `launch4j` for generating EXE.
- **Performance**: Compared to the Python version, it may offer faster startup speed and execution efficiency, with stricter dependency management.

### Usage
Directly double-click to run **`galsearch-java.exe`** in the root directory.
*Note: This program depends on Java 21+ environment. If JDK 21 is installed on your system, it will be used automatically; otherwise, you can configure JAVA_HOME.*

### Build Instructions (Development)
If you need to modify the code and repackage:

**Method 1: Use Auto Script (Recommended)**
1. Enter the `java` directory:
   ```powershell
   cd java
   ```
2. Run the build script:
   ```powershell
   .\build.ps1
   ```
   *The script will automatically check Maven and Java environment and execute the build.*

**Method 2: Manual Maven Build**
```powershell
cd java
mvn clean package
```
After building, the generated EXE file is located in `java/target/galgame-search.exe`.

---

## 📄 Disclaimer
- This tool is for learning and technical exchange only. Please do not use it for illegal purposes.
- Search results come from third-party websites; this tool does not store any resources.
