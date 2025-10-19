package com.pozocolorado.app.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ReservaRow {
    private final int id;
    private final StringProperty huesped = new SimpleStringProperty();
    private final StringProperty habitacion = new SimpleStringProperty();
    private final StringProperty desde = new SimpleStringProperty();
    private final StringProperty hasta = new SimpleStringProperty();
    private final StringProperty estado = new SimpleStringProperty();

    public ReservaRow(int id, String huesped, String habitacion, String desde, String hasta, String estado) {
        this.id = id;
        this.huesped.set(huesped);
        this.habitacion.set(habitacion);
        this.desde.set(desde);
        this.hasta.set(hasta);
        this.estado.set(estado);
    }

    public int getId(){ return id; }
    public StringProperty huespedProperty(){ return huesped; }
    public StringProperty habitacionProperty(){ return habitacion; }
    public StringProperty desdeProperty(){ return desde; }
    public StringProperty hastaProperty(){ return hasta; }
    public StringProperty estadoProperty(){ return estado; }
}
