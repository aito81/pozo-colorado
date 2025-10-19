package com.pozocolorado.app.controllers;

import com.pozocolorado.app.db.Database;
import com.pozocolorado.app.utils.AlertUtils;
import javafx.beans.value.ObservableValue;
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

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class ReservaController {

    @FXML private TableView<ReservaRow> table;
    @FXML private TableColumn<ReservaRow, String> colHuesped;
    @FXML private TableColumn<ReservaRow, String> colHabitacion;
    @FXML private TableColumn<ReservaRow, String> colDesde;
    @FXML private TableColumn<ReservaRow, String> colHasta;
    @FXML private TableColumn<ReservaRow, String> colEstado;

    @FXML private ComboBox<String> huespedBox;
    @FXML private TextField habitacionField;
    @FXML private DatePicker desdePicker;
    @FXML private DatePicker hastaPicker;
    @FXML private ComboBox<String> estadoBox;

    @FXML private ComboBox<String> filterHuespedBox;
    @FXML private DatePicker filterDesdePicker;
    @FXML private DatePicker filterHastaPicker;
    @FXML private TextField searchField;

    private final ObservableList<ReservaRow> data = FXCollections.observableArrayList();
    private Integer selectedId = null;

    @FXML
    public void initialize() {
        colHuesped.setCellValueFactory(c -> c.getValue().huespedProperty());
        colHabitacion.setCellValueFactory(c -> c.getValue().habitacionProperty());
        colDesde.setCellValueFactory(c -> c.getValue().desdeProperty());
        colHasta.setCellValueFactory(c -> c.getValue().hastaProperty());
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        estadoBox.setItems(FXCollections.observableArrayList("Reservado","Ocupado","Cancelado"));

        populateHuespedes();
        refresh();
        setupFiltering();

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                selectedId = val.getId();
                huespedBox.getSelectionModel().select(val.huespedProperty().get());
                habitacionField.setText(val.habitacionProperty().get());
                desdePicker.setValue(LocalDate.parse(val.desdeProperty().get()));
                hastaPicker.setValue(LocalDate.parse(val.hastaProperty().get()));
                estadoBox.getSelectionModel().select(val.estadoProperty().get());
            }
        });
    }

    private void populateHuespedes() {
        ObservableList<String> nombres = FXCollections.observableArrayList();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT nombre FROM persona ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) nombres.add(rs.getString(1));
        } catch (SQLException ex) { ex.printStackTrace(); }
        huespedBox.setItems(nombres);
        filterHuespedBox.setItems(FXCollections.observableArrayList(nombres));
    }

    private void setupFiltering() {
        FilteredList<ReservaRow> filtered = new FilteredList<>(data, p -> true);

        if (searchField != null) {
            searchField.textProperty().addListener((ObservableValue<? extends String> o, String ov, String nv) -> {
                String q = (nv == null) ? "" : nv.toLowerCase();
                filtered.setPredicate(row -> {
                    if (q.isBlank()) return true;
                    return row.huespedProperty().get().toLowerCase().contains(q)
                            || row.habitacionProperty().get().toLowerCase().contains(q)
                            || row.estadoProperty().get().toLowerCase().contains(q);
                });
            });
        }

        filterHuespedBox.valueProperty().addListener((o, ov, nv) -> applyFilter(filtered));
        filterDesdePicker.valueProperty().addListener((o, ov, nv) -> applyFilter(filtered));
        filterHastaPicker.valueProperty().addListener((o, ov, nv) -> applyFilter(filtered));

        SortedList<ReservaRow> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
    }

    private void applyFilter(FilteredList<ReservaRow> filtered) {
        filtered.setPredicate(row -> {
            LocalDate desde = filterDesdePicker.getValue();
            LocalDate hasta = filterHastaPicker.getValue();
            String huesped = filterHuespedBox.getValue();
            boolean match = true;
            if (huesped != null && !huesped.isBlank()) {
                match &= row.huespedProperty().get().equalsIgnoreCase(huesped);
            }
            if (desde != null) {
                match &= !LocalDate.parse(row.desdeProperty().get()).isBefore(desde);
            }
            if (hasta != null) {
                match &= !LocalDate.parse(row.hastaProperty().get()).isAfter(hasta);
            }
            return match;
        });
    }

    private void refresh() {
        data.clear();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT r.id, p.nombre, r.habitacion, r.desde, r.hasta, r.estado " +
                             "FROM reserva r JOIN persona p ON r.persona_id=p.id ORDER BY r.desde DESC")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.add(new ReservaRow(rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5), rs.getString(6)));
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    @FXML public void nueva(ActionEvent e) {
        selectedId = null;
        huespedBox.getSelectionModel().clearSelection();
        habitacionField.clear();
        desdePicker.setValue(null);
        hastaPicker.setValue(null);
        estadoBox.getSelectionModel().clearSelection();
        table.getSelectionModel().clearSelection();
    }

    @FXML public void guardar(ActionEvent e) {
        String huesped = huespedBox.getValue();
        if (huesped == null || habitacionField.getText().isBlank() ||
                desdePicker.getValue() == null || hastaPicker.getValue() == null ||
                estadoBox.getValue() == null) {
            AlertUtils.warn("Validación", "Complete todos los campos.");
            return;
        }

        int personaId = -1;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id FROM persona WHERE nombre=?")) {
            ps.setString(1, huesped);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) personaId = rs.getInt(1);
        } catch (SQLException ex) { ex.printStackTrace(); }

        if (personaId == -1) {
            AlertUtils.warn("Error", "No se encontró el huésped seleccionado.");
            return;
        }

        try (Connection c = Database.get()) {
            if (selectedId == null) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO reserva(persona_id,habitacion,desde,hasta,estado) VALUES(?,?,?,?,?)")) {
                    ps.setInt(1, personaId);
                    ps.setString(2, habitacionField.getText());
                    ps.setString(3, desdePicker.getValue().toString());
                    ps.setString(4, hastaPicker.getValue().toString());
                    ps.setString(5, estadoBox.getValue());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE reserva SET persona_id=?, habitacion=?, desde=?, hasta=?, estado=? WHERE id=?")) {
                    ps.setInt(1, personaId);
                    ps.setString(2, habitacionField.getText());
                    ps.setString(3, desdePicker.getValue().toString());
                    ps.setString(4, hastaPicker.getValue().toString());
                    ps.setString(5, estadoBox.getValue());
                    ps.setInt(6, selectedId);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        refresh(); nueva(null);
    }

    @FXML public void eliminar(ActionEvent e) {
        if (selectedId == null) {
            AlertUtils.warn("Validación", "Seleccione una reserva.");
            return;
        }
        if (!AlertUtils.confirm("Confirmación", "¿Eliminar la reserva seleccionada?")) return;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM reserva WHERE id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
        refresh(); nueva(null);
    }

    @FXML public void exportCsv(ActionEvent e) {
        try (FileWriter fw = new FileWriter("reservas_export.csv")) {
            fw.write("Huesped,Habitacion,Desde,Hasta,Estado\n");
            for (ReservaRow r : data) {
                fw.write(String.format("%s,%s,%s,%s,%s\n",
                        r.huespedProperty().get(),
                        r.habitacionProperty().get(),
                        r.desdeProperty().get(),
                        r.hastaProperty().get(),
                        r.estadoProperty().get()));
            }
            AlertUtils.info("Exportar CSV", "Archivo exportado como reservas_export.csv");
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML public void exportPdf(ActionEvent e) {
        AlertUtils.info("Exportar PDF", "Función de exportar PDF pendiente de implementación 😄");
    }

    @FXML public void goHuespedes(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/huespedes.fxml"));
        Parent root = loader.load();
        Scene current = table.getScene();
        Stage stage = (Stage) current.getWindow();
        root.getStylesheets().add(getClass().getResource("/bootstrapfx-core.css").toExternalForm());
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        root.getStyleClass().add(current.getRoot().getStyleClass().contains("dark") ? "dark" : "light");
        stage.setScene(new Scene(root, 1100, 680));
    }

    @FXML public void logout(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Parent root = loader.load();
        Scene current = table.getScene();
        Stage stage = (Stage) current.getWindow();
        root.getStylesheets().add(getClass().getResource("/bootstrapfx-core.css").toExternalForm());
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        root.getStyleClass().add(current.getRoot().getStyleClass().contains("dark") ? "dark" : "light");
        stage.setScene(new Scene(root, 800, 480));
    }
}
