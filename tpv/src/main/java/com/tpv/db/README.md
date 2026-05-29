Carpeta para gestionar la base de datos del proyecto.

- `DatabaseManager.java`: utilidad para obtener conexiones JDBC y ejecutar scripts SQL.
- `src/main/resources/db/sql/create-db-template.sql`: script de ejemplo para crear la base de datos y tablas.

Uso rápido para ejecutar el script (ejemplo):

```bash
mvn -Ddb.url=jdbc:mysql://localhost:3306 -Ddb.user=root -Ddb.password=pass exec:java -Dexec.mainClass="com.tpv.tools.RunSql"
```

(Opcionalmente, puedes usar la clase `DatabaseManager` desde tu código para ejecutar scripts.)
