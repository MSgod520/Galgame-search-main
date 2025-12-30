[**English**](README_EN.md) | **中文**

# Galgame Search Tool (Galgame 资源搜索工具)

这是一个用于搜索 Galgame 资源的桌面工具。本项目提供了两个版本：**Python 版本**（原版）和 **Java 版本**（重构版）。

## 📁 项目结构

- `python/`: Python 版本源代码
- `java/`: Java 版本源代码
- `galsearch-py.exe`: Python 版本预编译可执行文件
- `galsearch-java.exe`: Java 版本预编译可执行文件

---

## 🐍 Python 版本

### 原理
基于 Python 3 和 PySide6 (Qt for Python) 开发。
- **UI**: 使用 PySide6 构建现代化图形界面。
- **爬虫**: 使用 `requests` 库请求资源网站（如 Nyaa），解析 HTML 获取磁力链接。
- **功能**: 支持关键字搜索、结果列表展示、磁力链接复制/下载。

### 使用方法
直接双击运行根目录下的 **`galsearch-py.exe`**。

### 构建方法 (开发)
如果你需要修改代码并重新打包：
1. 进入 `python` 目录：
   ```powershell
   cd python
   ```
2. 安装依赖：
   ```powershell
   pip install -r requirements.txt
   ```
3. 使用 PyInstaller 打包：
   ```powershell
   pyinstaller --onefile --noconsole --name galsearch-py --icon=ico.ico --add-data "icon.png;." --clean --distpath .. main.py
   ```
   *(注意：打包后的 exe 会输出到上级目录，或者手动从 `dist` 移动)*

---

## ☕ Java 版本

### 原理
基于 Java 21 和 JavaFX 开发。
- **UI**: 使用 JavaFX 构建无边框窗口界面，CSS 进行美化。
- **构建**: 使用 Maven 管理依赖，`maven-shade-plugin` 打包依赖，`launch4j` 生成 EXE。
- **性能**: 相比 Python 版本，启动速度和运行效率可能更高，且依赖管理更加严格。

### 使用方法
直接双击运行根目录下的 **`galsearch-java.exe`**。
*注意：此程序依赖 Java 21+ 环境。如果你的系统已安装 JDK 21，它会自动使用；或者你可以配置 JAVA_HOME。*

### 构建方法 (开发)
如果你需要修改代码并重新打包：

**方法一：使用自动脚本 (推荐)**
1. 进入 `java` 目录：
   ```powershell
   cd java
   ```
2. 运行构建脚本：
   ```powershell
   .\build.ps1
   ```
   *脚本会自动检查 Maven 和 Java 环境并执行构建。*

**方法二：手动 Maven 构建**
```powershell
cd java
mvn clean package
```
构建完成后，生成的 EXE 文件位于 `java/target/galgame-search.exe`。

---

## 📄 注意事项
- 本工具仅用于学习和技术交流，请勿用于非法用途。
- 搜索结果来源于第三方网站，本工具不存储任何资源。
