package com.pozocolorado.app.db;

import java.sql.*;

public class Database {
    private static final String DB_FILE = "hotel.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;
    public static Connection get() throws SQLException { return DriverManager.getConnection(URL); }
    public static void init() {
        try (Connection c = get(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS usuario(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS persona(id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, tipo_documento TEXT, numero_documento TEXT, fecha_nacimiento TEXT, telefono TEXT, pais TEXT);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS reserva(id INTEGER PRIMARY KEY AUTOINCREMENT, persona_id INTEGER NOT NULL, habitacion TEXT NOT NULL, desde TEXT NOT NULL, hasta TEXT NOT NULL, estado TEXT NOT NULL, FOREIGN KEY(persona_id) REFERENCES persona(id) ON DELETE CASCADE);");
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM usuario")) {
                var rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement ins = c.prepareStatement("INSERT INTO usuario(username,password) VALUES('admin','admin')")) { ins.executeUpdate(); }
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}
