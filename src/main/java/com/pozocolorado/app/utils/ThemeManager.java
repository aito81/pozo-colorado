package com.pozocolorado.app.utils;

import javafx.stage.Stage;

public class ThemeManager {

    private static final String LIGHT_GRADIENT =
            "-fx-background-color: linear-gradient(to bottom right, #f7b733, #fc4a1a);";

    private static final String DARK_GRADIENT =
            "-fx-background-color: linear-gradient(to bottom right, #3a1c71, #d76d77, #ffaf7b);";

    public static void applyGradient(javafx.scene.Scene scene, boolean darkMode) {
        if (scene != null && scene.getRoot() instanceof javafx.scene.layout.Region root) {
            root.setStyle(darkMode ? DARK_GRADIENT : LIGHT_GRADIENT);
        }
    }

    /** 🖥️ Maximiza la ventana de forma segura y multiplataforma */
    public static void maximize(Stage stage) {
        if (stage != null) {
            stage.setMaximized(true);
        }
    }
}
