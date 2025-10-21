package com.pozocolorado.app.controllers;

import com.pozocolorado.app.utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox darkModeToggle;
    @FXML private ImageView logoView;
    @FXML private AnchorPane rootPane;


    @FXML
    public void initialize() {
        // 🌗 Listener del modo oscuro
        darkModeToggle.selectedProperty().addListener((obs, old, val) -> {
            Scene s = usernameField.getScene();
            if (s != null) {
                // Estilo de fondo
                if (val) {
                    rootPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #3a1c71, #d76d77, #ffaf7b);");
                } else {
                    rootPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #f7b733, #fc4a1a);");
                }

                // Tema general
                s.getRoot().getStyleClass().removeAll("light", "dark");
                s.getRoot().getStyleClass().add(val ? "dark" : "light");
            }
        });

        // ☀️ Cargar imagen del logo de forma segura
        try (InputStream logoStream = getClass().getResourceAsStream("/icons/sol.png")) {
            if (logoStream != null) {
                logoView.setImage(new Image(logoStream));
            } else {
                System.err.println("⚠️ Archivo no encontrado: /icons/sol.png");
            }
        } catch (Exception ex) {
            System.err.println("⚠️ Error cargando el logo: " + ex.getMessage());
        }

        // ✨ Animación fade-in suave del logo
        if (logoView != null && logoView.getImage() != null) {
            FadeTransition fade = new FadeTransition(Duration.seconds(1.8), logoView);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }
    }

    @FXML
    public void onLogin(ActionEvent e) throws IOException {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if ("admin".equalsIgnoreCase(user) && "admin".equals(pass)) {
            // ✅ Login correcto
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/huespedes.fxml"));
            Parent root = loader.load();
            Scene scene = usernameField.getScene();
            Stage stage = (Stage) scene.getWindow();

            // Cargar estilos
            root.getStylesheets().add(getClass().getResource("/bootstrapfx-core.css").toExternalForm());
            root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            // Mantener el tema actual (claro / oscuro)
            boolean dark = scene.getRoot().getStyleClass().contains("dark");

            Scene newScene = new Scene(root, 1100, 680);

// 🌈 Aplica el degradado del modo actual
            ThemeManager.applyGradient(newScene, dark);

            stage.setScene(newScene);
            ThemeManager.maximize(stage);
        } else {
            // 🚫 Credenciales incorrectas
            usernameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        }
    }
}
