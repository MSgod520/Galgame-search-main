from PySide6.QtWidgets import (QApplication, QMainWindow, QWidget, QVBoxLayout, 
                             QHBoxLayout, QLineEdit, QPushButton, QListWidget, 
                             QListWidgetItem, QLabel, QTextEdit, QMessageBox, QButtonGroup,
                             QScrollArea, QFrame, QSizePolicy)
from PySide6.QtCore import Qt, QThread, Signal
from PySide6.QtGui import QCursor, QIcon
from crawler import TouchGalCrawler
import sys, webbrowser, os

def resource_path(relative_path):
    """ Get absolute path to resource, works for dev and for PyInstaller """
    try:
        # PyInstaller creates a temp folder and stores path in _MEIPASS
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.abspath(".")

    return os.path.join(base_path, relative_path)

class SearchThread(QThread):
    finished = Signal(list)
    error = Signal(str)

    def __init__(self, crawler, keyword):
        super().__init__()
        self.crawler = crawler
        self.keyword = keyword

    def run(self):
        try:
            games = self.crawler.search_game(self.keyword)
            self.finished.emit(games)
        except Exception as e:
            self.error.emit(str(e))

class ResourceCard(QFrame):
    def __init__(self, resource):
        super().__init__()
        self.resource = resource
        self.setCursor(QCursor(Qt.PointingHandCursor))
        self.setStyleSheet("""
            ResourceCard {
                background-color: #2d2d44;
                border: 1px solid #4ecdc4;
                border-radius: 8px;
            }
            ResourceCard:hover {
                background-color: #3d3d5c;
                border: 1px solid #6ae6dd;
            }
            QLabel {
                border: none;
                background: transparent;
            }
        """)
        
        layout = QVBoxLayout()
        
        name_label = QLabel(resource.get('name', '未知资源'))
        name_label.setStyleSheet("color: white; font-weight: bold; font-size: 14px;")
        name_label.setWordWrap(True)
        
        link_text = resource.get('content', '')
        link_label = QLabel(link_text)
        link_label.setStyleSheet("color: #4ecdc4; font-size: 12px;")
        link_label.setWordWrap(True)
        
        layout.addWidget(name_label)
        layout.addWidget(link_label)
        
        pwd = resource.get('password')
        if pwd:
            pwd_label = QLabel(f"密码: {pwd}")
            pwd_label.setStyleSheet("color: #a0a0a0; font-size: 12px;")
            layout.addWidget(pwd_label)
            
        self.setLayout(layout)
        
    def mousePressEvent(self, event):
        url = self.resource.get('content')
        if url:
            webbrowser.open(url)

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Galgame 搜索工具")
        self.setWindowIcon(QIcon(resource_path("icon.png")))
        self.resize(900, 700)
        self.crawler = TouchGalCrawler()
        self.current_downloads = []
        self.current_category = "游戏本体"
        self.init_ui()
        self.setStyleSheet("""
            QMainWindow { background-color: #1a1a2e; }
            QLabel { color: #ffffff; font-size: 14px; }
            QLineEdit { background: #2d2d44; color: white; border: 1px solid #4ecdc4; padding: 8px; border-radius: 4px; }
            QPushButton { background: #4ecdc4; color: white; font-weight: bold; padding: 10px; border-radius: 4px; }
            QPushButton:hover { background: #6ae6dd; }
            QListWidget { background: #25253c; border: none; color: white; font-size: 16px; }
        """)

    def init_ui(self):
        main_layout = QVBoxLayout()
        
        # 搜索栏
        search_box = QHBoxLayout()
        self.search_input = QLineEdit()
        self.search_input.setPlaceholderText("请输入游戏名称...")
        self.search_input.returnPressed.connect(self.do_search) # 回车搜索
        self.btn_search = QPushButton("搜 索")
        self.btn_search.clicked.connect(self.do_search)
        search_box.addWidget(self.search_input)
        search_box.addWidget(self.btn_search)
        
        # 结果与详情
        content_box = QHBoxLayout()
        self.result_list = QListWidget()
        self.result_list.itemClicked.connect(self.show_detail)
        
        self.detail_area = QTextEdit() # Placeholder to keep interface, but we will not use it
        
        # 右侧面板布局
        right_layout = QVBoxLayout()
        
        # 1. 顶部信息区域 (这个区域不滚动，或者作为ScrollArea的第一部分)
        # 为了整体性，我们把所有东西都放进ScrollArea，或者把Image和Category固定
        # 用户要求："上方分类区靠右，在搜索框下面，搜索结果右边" -> 意味着在Right Panel顶部
        
        # 分类按钮 (从原位置移动到这里)
        category_box = QHBoxLayout()
        self.category_group = QButtonGroup(self)
        categories = ["游戏本体", "补丁资源", "存档资源"]
        for i, cat in enumerate(categories):
            btn = QPushButton(cat)
            btn.setCheckable(True)
            if i == 0: btn.setChecked(True)
            self.category_group.addButton(btn, i)
            category_box.addWidget(btn)
        self.category_group.idClicked.connect(self.change_category)
        
        # 滚动区域 (包含 简介 + 资源)
        self.scroll_area = QScrollArea()
        self.scroll_area.setWidgetResizable(True)
        self.scroll_area.setStyleSheet("""
            QScrollArea { border: none; background: transparent; }
            QWidget { background: transparent; }
            QScrollBar:vertical {
                border: none;
                background: #2d2d44;
                width: 10px;
                margin: 0px 0px 0px 0px;
            }
            QScrollBar::handle:vertical {
                background: #4ecdc4;
                min-height: 20px;
                border-radius: 5px;
            }
        """)
        
        self.content_container = QWidget()
        self.content_layout = QVBoxLayout()
        self.content_layout.setAlignment(Qt.AlignTop)
        self.content_container.setLayout(self.content_layout)
        self.scroll_area.setWidget(self.content_container)
        
        # 将组件加入 content_layout
        self.intro_label = QLabel()
        self.intro_label.setWordWrap(True)
        self.intro_label.setStyleSheet("color: #e0e0e0; font-size: 14px; padding: 10px;")
        self.intro_label.setTextInteractionFlags(Qt.TextSelectableByMouse)
        
        self.content_layout.addWidget(self.intro_label)
        self.content_layout.addSpacing(20) # 间距
        self.resource_start_index = 2 # 记录资源开始的index，方便清除
        
        # 组装 Right Layout
        # 组装 Right Layout
        right_layout.addLayout(category_box)
        right_layout.addWidget(self.scroll_area)
        
        right_widget = QWidget()
        right_widget.setLayout(right_layout)
        
        content_box.addWidget(self.result_list, 1)
        content_box.addWidget(right_widget, 2)
        
        main_layout.addLayout(search_box)
        main_layout.addLayout(content_box)
        
        container = QWidget()
        container.setLayout(main_layout)
        self.setCentralWidget(container)

    def do_search(self):
        # 防止重复搜索导致线程冲突
        if hasattr(self, 'search_thread') and self.search_thread.isRunning():
            return
            
        keyword = self.search_input.text()
        if not keyword: return
        
        # 禁用按钮防止重复提交
        self.btn_search.setEnabled(False)
        self.btn_search.setText("搜索中...")
        self.search_input.setEnabled(False)
        self.result_list.clear()
        
        self.search_thread = SearchThread(self.crawler, keyword)
        self.search_thread.finished.connect(self.on_search_finished)
        self.search_thread.error.connect(self.on_search_error)
        self.search_thread.start()

    def on_search_finished(self, games):
        self.games = games
        for game in self.games:
            self.result_list.addItem(game['name'])
        
        # 恢复界面状态
        self.btn_search.setEnabled(True)
        self.btn_search.setText("搜 索")
        self.search_input.setEnabled(True)
        if not games:
            QMessageBox.information(self, "提示", "未找到相关游戏")

    def on_search_error(self, err):
        QMessageBox.critical(self, "错误", f"请求失败: {err}")
        self.btn_search.setEnabled(True)
        self.btn_search.setText("搜 索")
        self.search_input.setEnabled(True)

    def change_category(self, id):
        buttons = self.category_group.buttons()
        self.current_category = buttons[id].text()
        self.update_detail_view()

    def show_detail(self, item):
        try:
            game = next(g for g in self.games if g['name'] == item.text())
            self.current_game_info = game
            self.current_downloads = self.crawler.get_downloads(game['id'])
            self.update_detail_view()
        except Exception as e:
            QMessageBox.warning(self, "获取详情失败", str(e))

    def update_detail_view(self):
        if not hasattr(self, 'current_game_info'): return
        
        game = self.current_game_info
        # 更新简介
        intro_text = f"【名称】：{game['name']}\n"
        intro_text += f"【平台】：{', '.join(game.get('platform', []))}\n"
        intro_text += f"【简介】：{game.get('introduction', '无')}\n"
        self.intro_label.setText(intro_text)
        
        # 清空旧资源 (保留前面固定的intro组件)
        while self.content_layout.count() > 1: # 0 is intro_label
            item = self.content_layout.takeAt(1)
            widget = item.widget()
            if widget: widget.deleteLater()
            else:
                 # It might be spacing or layout
                 pass
        
        # 添加分割线效果
        line = QFrame()
        line.setFrameShape(QFrame.HLine)
        line.setFrameShadow(QFrame.Sunken)
        line.setStyleSheet("background-color: #4ecdc4;")
        self.content_layout.addWidget(line)
        
        filtered_res = []
        for res in self.current_downloads:
            name = res.get('name', '')
            if self.current_category == "补丁资源":
                if "补丁" in name: filtered_res.append(res)
            elif self.current_category == "存档资源":
                if "存档" in name: filtered_res.append(res)
            else: # 游戏本体
                if "补丁" not in name and "存档" not in name: filtered_res.append(res)
        
        # 排序逻辑
        # 3. 没有名字/没有链接排最后面 (Score 0)
        # 2. 有链接有密码排最前面 (Score 2)
        # 1. 有链接无密码排中间 (Score 1)
        def sort_key(res):
            name = res.get('name')
            content = res.get('content')
            pwd = res.get('password')
            
            if not name or not content:
                return 0
            if pwd:
                return 2
            return 1
            
        filtered_res.sort(key=sort_key, reverse=True)
        
        if not filtered_res:
            lbl = QLabel("该游戏目前空空如也哦")
            lbl.setStyleSheet("color: #a0a0a0; font-size: 16px; padding: 20px;")
            lbl.setAlignment(Qt.AlignCenter)
            self.content_layout.addWidget(lbl)
        else:
            for res in filtered_res:
                card = ResourceCard(res)
                self.content_layout.addWidget(card)

if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = MainWindow()
    window.show()
    sys.exit(app.exec())