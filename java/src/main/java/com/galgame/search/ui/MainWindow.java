package com.galgame.search.ui;

import com.galgame.search.model.Game;
import com.galgame.search.model.Resource;
import com.galgame.search.service.AggregatedService;
import com.galgame.search.service.TouchGalService;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MainWindow extends BorderPane {

    private final AggregatedService service;
    private final TextField searchField;
    private final Button searchButton;
    private final ListView<Game> resultList;
    private final VBox detailContent;
    private final ToggleGroup categoryGroup;
    private String currentCategory = "游戏本体";
    private Game currentGame;

    private final Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;

    public MainWindow(Stage stage) {
        this.stage = stage;
        this.service = new AggregatedService();
        this.getStyleClass().add("main-window");
        
        // --- Top: Custom Title Bar ---
        HBox topBox = new HBox(15);
        topBox.setPadding(new Insets(10, 20, 10, 20));
        topBox.setAlignment(Pos.CENTER_LEFT); // Align items to left, but we will use spacer
        topBox.getStyleClass().add("top-bar");
        
        // Window Dragging Logic
        topBox.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        topBox.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Search Field & Button (Center)
        searchField = new TextField();
        searchField.setId("search-field");
        searchField.setPromptText("搜索 Galgame...");
        searchField.setPrefWidth(350);
        searchField.setPrefHeight(35);
        
        searchButton = new Button("搜 索");
        searchButton.setId("search-button");
        searchButton.setPrefHeight(35);
        
        // Spacer to push window controls to right
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        
        // Region rightSpacer = new Region();
        // HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        // We want search bar centered? Or left?
        // Let's keep search bar roughly centered or left-aligned.
        // User screenshot shows centered search bar.
        
        // Title Label (Optional, maybe "Galgame Search" text on far left)
        Label titleLabel = new Label("Galgame 搜索");
        titleLabel.setStyle("-fx-text-fill: #565f89; -fx-font-weight: bold;");
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Window Controls
        HBox windowControls = new HBox(8);
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        
        Button minBtn = new Button("—");
        minBtn.getStyleClass().add("win-btn");
        minBtn.setOnAction(e -> stage.setIconified(true));
        
        Button maxBtn = new Button("□");
        maxBtn.getStyleClass().add("win-btn");
        maxBtn.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        
        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("win-btn");
        closeBtn.setId("close-btn");
        closeBtn.setOnAction(e -> Platform.exit());
        
        windowControls.getChildren().addAll(minBtn, maxBtn, closeBtn);

        // Layout: [Title] [Spacer] [Search Input] [Search Btn] [Spacer] [Controls]
        topBox.getChildren().addAll(titleLabel, spacer1, searchField, searchButton, spacer2, windowControls);
        this.setTop(topBox);

        // --- Center: SplitPane (Results | Details) ---
        SplitPane splitPane = new SplitPane();
        splitPane.getStyleClass().add("main-split-pane");
        
        // 1. Result List (Left)
        VBox leftPane = new VBox();
        leftPane.getStyleClass().add("left-pane");
        
        Label resultLabel = new Label("搜索结果");
        resultLabel.getStyleClass().add("section-header");
        
        resultList = new ListView<>();
        resultList.setId("result-list");
        resultList.setCellFactory(lv -> new ListCell<Game>() {
            @Override
            protected void updateItem(Game item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.name() + "\n[" + item.source() + "]");
                    getStyleClass().add("game-cell");
                }
            }
        });
        
        VBox.setVgrow(resultList, Priority.ALWAYS);
        leftPane.getChildren().addAll(resultLabel, resultList);

        // 2. Details (Right)
        VBox rightPane = new VBox(15);
        rightPane.getStyleClass().add("right-pane");
        rightPane.setPadding(new Insets(15));
        
        // Category Buttons
        HBox categoryBox = new HBox(15);
        categoryBox.setId("category-box");
        categoryBox.setAlignment(Pos.CENTER_LEFT);
        categoryGroup = new ToggleGroup();
        
        String[] cats = {"游戏本体", "补丁资源", "存档资源"};
        for (String cat : cats) {
            ToggleButton btn = new ToggleButton(cat);
            btn.setToggleGroup(categoryGroup);
            btn.setUserData(cat);
            btn.getStyleClass().add("category-btn");
            if (cat.equals(currentCategory)) btn.setSelected(true);
            categoryBox.getChildren().add(btn);
        }
        
        categoryGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentCategory = (String) newVal.getUserData();
                updateResourcesView();
            }
        });

        // Scroll Area for details
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setId("detail-scroll");
        
        detailContent = new VBox(20); // Spacing for content
        detailContent.setId("detail-content");
        
        scrollPane.setContent(detailContent);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        rightPane.getChildren().addAll(categoryBox, scrollPane);

        // Add to SplitPane
        splitPane.getItems().addAll(leftPane, rightPane);
        splitPane.setDividerPositions(0.3); // 30% width for list
        
        this.setCenter(splitPane); // Set SplitPane as Center

        // --- Event Handlers ---
        searchButton.setOnAction(e -> doSearch());
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) doSearch();
        });
        
        resultList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showDetail(newVal);
            }
        });
    }

    private void doSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;

        searchButton.setDisable(true);
        searchButton.setText("搜索中...");
        resultList.getItems().clear();
        detailContent.getChildren().clear();

        Task<List<Game>> task = new Task<>() {
            @Override
            protected List<Game> call() throws Exception {
                return service.searchAll(keyword);
            }
        };

        task.setOnSucceeded(e -> {
            List<Game> games = task.getValue();
            if (games.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "未找到相关游戏");
                alert.initOwner(this.getScene().getWindow());
                alert.show();
            } else {
                resultList.getItems().addAll(games);
            }
            searchButton.setDisable(false);
            searchButton.setText("搜 索");
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            Alert alert = new Alert(Alert.AlertType.ERROR, "请求失败: " + ex.getMessage());
            alert.initOwner(this.getScene().getWindow());
            alert.show();
            searchButton.setDisable(false);
            searchButton.setText("搜 索");
        });

        new Thread(task).start();
    }

    private void showDetail(Game game) {
        if (game == null) return;
        currentGame = game;
        
        // Display Info Immediately
        detailContent.getChildren().clear();
        
        Label title = new Label("【名称】：" + game.name());
        title.getStyleClass().add("header-label");
        
        Label platform = new Label("【平台】：" + String.join(", ", game.platform()));
        platform.getStyleClass().add("content-label");
        
        Label intro = new Label("【简介】：" + (game.introduction() == null ? "无" : game.introduction()));
        intro.getStyleClass().add("content-label");
        intro.setWrapText(true);
        
        detailContent.getChildren().addAll(title, platform, intro, new Separator());
        
        // Fetch resources asynchronously
        Label loading = new Label("加载资源中...");
        loading.getStyleClass().add("sub-label");
        detailContent.getChildren().add(loading);

        Task<List<Resource>> task = new Task<>() {
            @Override
            protected List<Resource> call() throws Exception {
                return service.getDownloads(game);
            }
        };

        task.setOnSucceeded(e -> {
            // Remove loading label (last item)
            detailContent.getChildren().remove(loading);
            // Store resources in userData or similar, or just re-render is fine.
            // But we need to support category switching on the same set of resources.
            // So let's store them in the MainWindow state or pass them around.
            // For simplicity, let's store in a field (need to add it)
            currentResources = task.getValue();
            updateResourcesView();
        });
        
         task.setOnFailed(e -> {
            detailContent.getChildren().remove(loading);
            Label err = new Label("加载资源失败: " + task.getException().getMessage());
            err.setStyle("-fx-text-fill: red;");
            detailContent.getChildren().add(err);
        });

        new Thread(task).start();
    }
    
    private List<Resource> currentResources = Collections.emptyList();

    private void updateResourcesView() {
        if (currentGame == null) return;

        // Clear only resources ie items after the Separator
        // The separator is the 4th item (index 3). title, plat, intro, sep.
        // So clear from index 4.
        if (detailContent.getChildren().size() > 4) {
            detailContent.getChildren().remove(4, detailContent.getChildren().size());
        }

        if (currentResources == null || currentResources.isEmpty()) {
            Label placeholder = new Label("暂无资源");
            placeholder.getStyleClass().add("sub-label");
            detailContent.getChildren().add(placeholder);
            return;
        }

        // Filter
        List<Resource> filtered = currentResources.stream().filter(res -> {
            String name = res.name() == null ? "" : res.name();
            if ("补丁资源".equals(currentCategory)) {
                return name.contains("补丁");
            } else if ("存档资源".equals(currentCategory)) {
                return name.contains("存档");
            } else { // 游戏本体
                return !name.contains("补丁") && !name.contains("存档");
            }
        }).collect(Collectors.toList());

        // Sort
        // 1. (Link & Pwd) -> Priority
        // 2. Link -> Normal
        // 3. No Name or Link -> Last
        filtered.sort((r1, r2) -> {
           int score1 = getScore(r1);
           int score2 = getScore(r2);
           return Integer.compare(score2, score1); // Descending
        });

        if (filtered.isEmpty()) {
             Label placeholder = new Label("该分类下暂无资源");
             placeholder.getStyleClass().add("sub-label");
             detailContent.getChildren().add(placeholder);
        } else {
            for (Resource res : filtered) {
                detailContent.getChildren().add(new ResourceCard(res));
            }
        }
    }

    private int getScore(Resource r) {
        boolean hasName = r.name() != null && !r.name().isEmpty();
        String content = r.getContentString();
        boolean hasLink = content != null && !content.isEmpty();
        boolean hasPwd = r.password() != null && !r.password().isEmpty();

        if (!hasName || !hasLink) return 0;
        
        // Priority 1: Official TouchGal Pan
        if (content.contains("pan.touchgal.net")) return 10;
        
        // Priority 2: Link + Password
        if (hasPwd) return 2;
        
        // Priority 3: Link only
        return 1;
    }
}
