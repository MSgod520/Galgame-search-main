package com.galgame.search;

import com.galgame.search.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Frameless window
        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        
        MainWindow root = new MainWindow(stage); // Pass stage to MainWindow for drag/close
        Scene scene = new Scene(root, 900, 700);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT); // Transparent scene background
        
        // Load CSS
        try {
            String css = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Failed to load CSS: " + e.getMessage());
        }

        // Set Icon
        try {
            // Trying to use the icon from the python folder if we can't find one in resources yet.
            // Beacuse we didn't move the icon.png to resources.
            // Let's try to load it from file system if resource fails, or just omit if tricky.
            // Ideally we should have copied icon.png to src/main/resources.
            // I'll assume standard resource loading first.
            InputStream iconStream = getClass().getResourceAsStream("/icon.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("Failed to load Icon: " + e.getMessage());
        }

        stage.setTitle("Galgame 搜索工具 (Java Edition)");
        stage.setScene(scene);
        stage.show();
    }
}
