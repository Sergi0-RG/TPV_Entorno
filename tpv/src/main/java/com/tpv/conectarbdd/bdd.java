package com.tpv.conectarbdd;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Bdd {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/minitpv?useSSL=false&serverTimezone=UTC";    
    private static final String DEFAULT_USER     = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String PROPERTIES_FILE  = "db.properties";

    private static Bdd instance;

    private Connection connection;
    private String url;
    private String user;
    private String password;

    private Bdd() {
        loadProperties();
    }

    public static synchronized Bdd getInstance() {
        if (instance == null) {
            instance = new Bdd();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[DB] Conexión cerrada correctamente.");
                }
            } catch (SQLException e) {
                System.err.println("[DB] Error al cerrar la conexión: " + e.getMessage());
            } finally {
                connection = null;
                instance   = null;
            }
        }
    }

    private void loadProperties() {
        Properties props = new Properties();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (is != null) {
                props.load(is);
                url      = props.getProperty("db.url",      DEFAULT_URL);
                user     = props.getProperty("db.user",     DEFAULT_USER);
                password = props.getProperty("db.password", DEFAULT_PASSWORD);
                System.out.println("[DB] Configuración cargada desde " + PROPERTIES_FILE);
            } else {
                System.out.println("[DB] " + PROPERTIES_FILE + " no encontrado. Usando valores por defecto.");
                url      = DEFAULT_URL;
                user     = DEFAULT_USER;
                password = DEFAULT_PASSWORD;
            }
        } catch (IOException e) {
            System.err.println("[DB] Error leyendo " + PROPERTIES_FILE + ": " + e.getMessage());
            url      = DEFAULT_URL;
            user     = DEFAULT_USER;
            password = DEFAULT_PASSWORD;
        }
    }
    private void connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "[DB] Driver MySQL no encontrado. Añade mysql-connector-j-xx.jar al classpath.", e
            );
        }

        connection = DriverManager.getConnection(url, user, password);
        System.out.println("[DB] Conexión establecida con: " + url);
    }
}