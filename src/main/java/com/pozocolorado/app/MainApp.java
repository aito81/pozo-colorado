package com.pozocolorado.app;

import com.pozocolorado.app.utils.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import com.pozocolorado.app.db.Database;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Database.init();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 480);
        scene.getRoot().getStyleClass().add("light");
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/bootstrapfx-core.css").toExternalForm());
        stage.setTitle("Hotel Pozo Colorado");
        try { stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/sol.png"))); } catch (Exception ignore) {}
        stage.setScene(scene);
        stage.show();
        ThemeManager.maximize(stage);
    }
    public static void main(String[] args) { launch(args); }
}
