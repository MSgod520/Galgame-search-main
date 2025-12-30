package com.galgame.search.ui;

import com.galgame.search.model.Resource;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.awt.Desktop;
import java.net.URI;

public class ResourceCard extends VBox {

    public ResourceCard(Resource resource) {
        this.getStyleClass().add("resource-card");
        this.setSpacing(5);

        Label nameLabel = new Label(resource.name() != null ? resource.name() : "未知资源");
        nameLabel.getStyleClass().add("header-label");
        nameLabel.setWrapText(true);
        this.getChildren().add(nameLabel);

        // Parse content for multiple links
        String content = resource.getContentString();
        if (content != null && !content.isEmpty()) {
            // Split by comma (common separator) or just treat the whole thing as one if no comma
            // The user input suggests comma separation: url1,url2
            // We also handle spaces just in case
            String[] parts = content.split("[,\\s]+"); // Split by comma or whitespace sequences
            
            for (String part : parts) {
                if (part.startsWith("http")) {
                    Label linkLabel = new Label(part);
                    linkLabel.getStyleClass().add("link-label");
                    linkLabel.setWrapText(true);
                    linkLabel.setStyle("-fx-cursor: hand;");
                    
                    linkLabel.setOnMouseClicked(e -> {
                        openUrl(part, resource.password());
                        e.consume(); // Prevent bubbling if we keep card click
                    });
                    this.getChildren().add(linkLabel);
                }
            }
        }

        if (resource.password() != null && !resource.password().isEmpty()) {
            Label pwdLabel = new Label("密码: " + resource.password());
            pwdLabel.getStyleClass().add("sub-label");
            
            // Allow copying password on click
            pwdLabel.setOnMouseClicked(e -> {
                ClipboardContent clipboard = new ClipboardContent();
                clipboard.putString(resource.password());
                Clipboard.getSystemClipboard().setContent(clipboard);
            });
            pwdLabel.setStyle("-fx-cursor: hand;"); 
            
            this.getChildren().add(pwdLabel);
        }
    }

    private void openUrl(String rawUrl, String password) {
         try {
            String url = rawUrl;
            // Auto-fill password for Baidu Pan
            if (url.contains("pan.baidu.com") && password != null && !password.isEmpty()) {
                // Check if URL already has parameters
                if (url.contains("?")) {
                    url += "&pwd=" + password;
                } else {
                    url += "?pwd=" + password;
                }
            }
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
