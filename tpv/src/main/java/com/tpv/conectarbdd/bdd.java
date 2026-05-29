package com.tpv.conectarbdd;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestiona la conexión a la base de datos MySQL.
 * Implementa el patrón Singleton para reutilizar la misma conexión
 * durante toda la ejecución de la aplicación.
 *
 * <p>La configuración se lee desde {@code db.properties} ubicado en
 * {@code src/main/resources/}. Si no existe, se usan los valores
 * por defecto definidos en esta clase.</p>
 *
 * <p>Uso típico:</p>
 * <pre>
 *   Connection conn = bdd.getInstance().getConnection();
 * </pre>
 *
 * @author Sergio Ropero
 */
public class Bdd {

    // ----------------------------------------------------------------
    //  Valores por defecto (se sobreescriben con db.properties)
    // ----------------------------------------------------------------
    private static final String DEFAULT_URL      = "jdbc:mysql://localhost:3306/tpv_db";
    private static final String DEFAULT_USER     = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String PROPERTIES_FILE  = "db.properties";

    // ----------------------------------------------------------------
    //  Singleton
    // ----------------------------------------------------------------
    private static Bdd instance;

    private Connection connection;
    private String url;
    private String user;
    private String password;

    // ----------------------------------------------------------------
    //  Constructor privado
    // ----------------------------------------------------------------
    private Bdd() {
        loadProperties();
    }

    // ----------------------------------------------------------------
    //  Obtener instancia única
    // ----------------------------------------------------------------

    /**
     * Devuelve la única instancia de {@code bdd}.
     * Crea la instancia la primera vez que se llama (lazy initialization).
     *
     * @return instancia singleton
     */
    public static synchronized Bdd getInstance() {
        if (instance == null) {
            instance = new Bdd();
        }
        return instance;
    }

    // ----------------------------------------------------------------
    //  Métodos públicos
    // ----------------------------------------------------------------

    /**
     * Devuelve una conexión activa a MySQL.
     * Si la conexión está cerrada o es nula, abre una nueva.
     *
     * @return {@link Connection} lista para usar
     * @throws SQLException si no se puede establecer la conexión
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    /**
     * Cierra la conexión y destruye la instancia singleton,
     * permitiendo que se cree una nueva si fuera necesario.
     */
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

    // ----------------------------------------------------------------
    //  Métodos privados
    // ----------------------------------------------------------------

    /**
     * Carga los parámetros de conexión desde {@code db.properties}.
     * Si el fichero no existe, usa los valores por defecto.
     */
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

    /**
     * Abre la conexión física con MySQL.
     *
     * @throws SQLException si el driver no está disponible o los datos son incorrectos
     */
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