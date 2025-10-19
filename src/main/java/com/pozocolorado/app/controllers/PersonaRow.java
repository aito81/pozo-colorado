package com.pozocolorado.app.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PersonaRow {
    private final int id;
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty tipoDocumento = new SimpleStringProperty();
    private final StringProperty numeroDocumento = new SimpleStringProperty();
    private final StringProperty fechaNacimiento = new SimpleStringProperty();
    private final StringProperty telefono = new SimpleStringProperty();
    private final StringProperty pais = new SimpleStringProperty();

    public PersonaRow(int id, String nombre, String tipoDoc, String numDoc, String fechaNac, String telefono, String pais) {
        this.id = id;
        this.nombre.set(nombre);
        this.tipoDocumento.set(tipoDoc);
        this.numeroDocumento.set(numDoc);
        this.fechaNacimiento.set(fechaNac);
        this.telefono.set(telefono);
        this.pais.set(pais);
    }

    public int getId(){ return id; }
    public String getNombre(){ return nombre.get(); }
    public StringProperty nombreProperty(){ return nombre; }
    public String getTipoDocumento(){ return tipoDocumento.get(); }
    public StringProperty tipoDocumentoProperty(){ return tipoDocumento; }
    public String getNumeroDocumento(){ return numeroDocumento.get(); }
    public StringProperty numeroDocumentoProperty(){ return numeroDocumento; }
    public String getFechaNacimiento(){ return fechaNacimiento.get(); }
    public StringProperty fechaNacimientoProperty(){ return fechaNacimiento; }
    public String getTelefono(){ return telefono.get(); }
    public StringProperty telefonoProperty(){ return telefono; }
    public String getPais(){ return pais.get(); }
    public StringProperty paisProperty(){ return pais; }
}
