package com.pozocolorado.app.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox darkModeToggle;

    @FXML public void initialize() {
        darkModeToggle.selectedProperty().addListener((obs, old, val) -> {
            Scene s = usernameField.getScene();
            if (s != null) {
                s.getRoot().getStyleClass().removeAll("light","dark");
                s.getRoot().getStyleClass().add(val ? "dark" : "light");
            }
        });
    }

    @FXML public void onLogin(ActionEvent e) throws IOException {
        String u = usernameField.getText();
        String p = passwordField.getText();
        if ("admin".equals(u) && "admin".equals(p)) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/huespedes.fxml"));
            Parent root = loader.load();
            Scene scene = usernameField.getScene();
            Stage stage = (Stage) scene.getWindow();
            root.getStylesheets().add(getClass().getResource("/bootstrapfx-core.css").toExternalForm());
            root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            root.getStyleClass().add(scene.getRoot().getStyleClass().contains("dark") ? "dark" : "light");
            stage.setScene(new Scene(root, 1100, 680));
        } else {
            usernameField.setStyle("-fx-border-color: red;");
            passwordField.setStyle("-fx-border-color: red;");
        }
    }
}
