package com.tpv.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utilidad simple para ejecutar scripts SQL y obtener conexiones JDBC.
 * Lee parámetros de conexión desde las propiedades del sistema:
 *  - db.url
 *  - db.user
 *  - db.password
 */
public class DatabaseManager {

    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager() {
        this.url = System.getProperty("db.url", "jdbc:mysql://localhost:3306/minitpv?useSSL=false&serverTimezone=UTC");
        this.user = System.getProperty("db.user", "root");
        this.password = System.getProperty("db.password", "");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Ejecuta un archivo SQL simple. Se divide por punto y coma (';') y ejecuta cada sentencia.
     */
    public void runSqlScript(Path sqlFile) throws IOException, SQLException {
        String sql = Files.readString(sqlFile);
        String[] statements = sql.split("(?<=;)\\s*\n?");
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            for (String stmt : statements) {
                stmt = stmt.trim();
                if (stmt.isEmpty()) continue;
                // Quitar el ; final si existe
                if (stmt.endsWith(";")) stmt = stmt.substring(0, stmt.length() - 1);
                if (stmt.isEmpty()) continue;
                st.execute(stmt);
            }
        }
    }
}
