package com.pozocolorado.app.controllers;

import com.pozocolorado.app.db.Database;
import com.pozocolorado.app.utils.AlertUtils;
import com.pozocolorado.app.utils.ThemeManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class HuespedController {

    @FXML private TableView<PersonaRow> table;
    @FXML private TableColumn<PersonaRow, String> colNombre;
    @FXML private TableColumn<PersonaRow, String> colTipoDoc;
    @FXML private TableColumn<PersonaRow, String> colNumeroDoc;
    @FXML private TableColumn<PersonaRow, String> colNacimiento;
    @FXML private TableColumn<PersonaRow, String> colTelefono;
    @FXML private TableColumn<PersonaRow, String> colPais;

    @FXML private TextField nombreField;
    @FXML private ComboBox<String> tipoDocBox;
    @FXML private TextField numeroDocField;
    @FXML private DatePicker nacimientoPicker;
    @FXML private TextField telefonoField;
    @FXML private TextField paisField;
    @FXML private TextField searchField;

    private final ObservableList<PersonaRow> data = FXCollections.observableArrayList();
    private Integer selectedId = null;

    @FXML public void initialize() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = table.getScene();
            if (scene != null) {
                ThemeManager.applyGradient(scene, false);

                // 💥 Forzamos la ventana al 100%
                Stage stage = (Stage) scene.getWindow();
                if (stage != null) {
                    stage.setMaximized(false);
                    stage.setMaximized(true);
                }
            }
        });
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colTipoDoc.setCellValueFactory(c -> c.getValue().tipoDocumentoProperty());
        colNumeroDoc.setCellValueFactory(c -> c.getValue().numeroDocumentoProperty());
        colNacimiento.setCellValueFactory(c -> c.getValue().fechaNacimientoProperty());
        colTelefono.setCellValueFactory(c -> c.getValue().telefonoProperty());
        colPais.setCellValueFactory(c -> c.getValue().paisProperty());
        tipoDocBox.setItems(FXCollections.observableArrayList("CI","DNI","Pasaporte","Otro"));

        FilteredList<PersonaRow> filtered = new FilteredList<>(data, p -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((o,ov,nv)->{
                String q = nv == null ? "" : nv.toLowerCase();
                filtered.setPredicate(row -> {
                    if (q.isBlank()) return true;
                    return (row.getNombre()!=null && row.getNombre().toLowerCase().contains(q)) ||
                           (row.getNumeroDocumento()!=null && row.getNumeroDocumento().toLowerCase().contains(q)) ||
                           (row.getPais()!=null && row.getPais().toLowerCase().contains(q));
                });
            });
        }
        SortedList<PersonaRow> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                selectedId = val.getId();
                nombreField.setText(val.getNombre());
                tipoDocBox.getSelectionModel().select(val.getTipoDocumento());
                numeroDocField.setText(val.getNumeroDocumento());
                nacimientoPicker.setValue(val.getFechaNacimiento() == null || val.getFechaNacimiento().isBlank() ? null : LocalDate.parse(val.getFechaNacimiento()));
                telefonoField.setText(val.getTelefono());
                paisField.setText(val.getPais());
            }
        });
        refresh();

        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) table.getScene().getWindow();
            if (stage != null) {
                stage.setMaximized(false);
                stage.setMaximized(true);
            }
        });
    }

    private void refresh() {
        data.clear();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id,nombre,tipo_documento,numero_documento,fecha_nacimiento,telefono,pais FROM persona ORDER BY nombre")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.add(new PersonaRow(
                        rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getString(7)
                    ));
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    @FXML public void nuevo(ActionEvent e) {
        selectedId = null;
        nombreField.clear(); tipoDocBox.getSelectionModel().clearSelection();
        numeroDocField.clear(); nacimientoPicker.setValue(null);
        telefonoField.clear(); paisField.clear();
        table.getSelectionModel().clearSelection();
    }

    @FXML public void guardar(ActionEvent e) {
        String nombre = nombreField.getText();
        String tipo = tipoDocBox.getValue();
        String num = numeroDocField.getText();
        java.time.LocalDate nac = nacimientoPicker.getValue();
        String tel = telefonoField.getText();
        String pais = paisField.getText();
        if (nombre == null || nombre.isBlank()) { AlertUtils.warn("Validación", "El nombre es obligatorio."); return; }
        try (Connection c = Database.get()) {
            if (selectedId == null) {
                try (PreparedStatement ps = c.prepareStatement(
                  "INSERT INTO persona(nombre,tipo_documento,numero_documento,fecha_nacimiento,telefono,pais) VALUES(?,?,?,?,?,?)")) {
                    ps.setString(1, nombre); ps.setString(2, tipo); ps.setString(3, num);
                    ps.setString(4, nac == null ? null : nac.toString()); ps.setString(5, tel); ps.setString(6, pais);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                  "UPDATE persona SET nombre=?, tipo_documento=?, numero_documento=?, fecha_nacimiento=?, telefono=?, pais=? WHERE id=?")) {
                    ps.setString(1, nombre); ps.setString(2, tipo); ps.setString(3, num);
                    ps.setString(4, nac == null ? null : nac.toString()); ps.setString(5, tel); ps.setString(6, pais); ps.setInt(7, selectedId);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        refresh(); nuevo(null);
    }

    @FXML public void eliminar(ActionEvent e) {
        if (selectedId == null) { AlertUtils.warn("Validación", "Seleccione un huésped."); return; }
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM persona WHERE id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
        refresh(); nuevo(null);
    }

    @FXML public void goReservas(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reservas.fxml"));
        Parent root = loader.load();
        Scene current = table.getScene();
        Stage stage = (Stage) current.getWindow();
        root.getStylesheets().add(getClass().getResource("/bootstrapfx-core.css").toExternalForm());
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        boolean dark = current.getRoot().getStyleClass().contains("dark");

        Scene newScene = new Scene(root);
        ThemeManager.applyGradient(newScene, dark);
        stage.setScene(newScene);

        // 👇 Este bloque fuerza el remaximizado de manera segura
        javafx.application.Platform.runLater(() -> {
            stage.setMaximized(false); // fuerza recalcular layout
            stage.setMaximized(true);  // vuelve a pantalla completa
        });
    }

    @FXML public void logout(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Parent root = loader.load();
        Scene current = table.getScene();
        Stage stage = (Stage) current.getWindow();
        root.getStylesheets().add(getClass().getResource("/bootstrapfx-core.css").toExternalForm());
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        boolean dark = current.getRoot().getStyleClass().contains("dark");

        Scene newScene = new Scene(root);
        ThemeManager.applyGradient(newScene, dark);
        stage.setScene(newScene);

        // 👇 Igual que arriba
        javafx.application.Platform.runLater(() -> {
            stage.setMaximized(false);
            stage.setMaximized(true);
        });
    }
    @FXML
    public void exportarCSV(ActionEvent e) {
        AlertUtils.info("Exportar CSV", "Esta función estará disponible próximamente.");
    }

    @FXML
    public void exportarPDF(ActionEvent e) {
        AlertUtils.info("Exportar PDF", "Esta función estará disponible próximamente.");
    }

}
