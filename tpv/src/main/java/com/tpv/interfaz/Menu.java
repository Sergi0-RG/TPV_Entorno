package com.tpv.interfaz;

import com.tpv.DAOs.productosDAO;
import com.tpv.DAOs.VentasDAO;
import com.tpv.conectarbdd.Bdd;
import com.tpv.modelos.Productos;
import com.tpv.modelos.Ventas;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Menu {

    private final Scanner scanner;
    private final productosDAO prodDAO;
    private final VentasDAO ventDAO;

    // Clase interna auxiliar para manejar el carrito de compras
    private static class ItemCarrito {
        Productos producto;
        int cantidad;
        double totalLinea;

        ItemCarrito(Productos p, int cantidad) {
            this.producto = p;
            this.cantidad = cantidad;
            this.totalLinea = p.getPrecio() * cantidad;
        }
    }

    public Menu() {
        this.scanner = new Scanner(System.in);
        this.prodDAO = new productosDAO();
        this.ventDAO = new VentasDAO();
    }

    public void iniciar() {
        boolean salir = false;

        while (!salir) {
            System.out.println("\n====================================");
            System.out.println("         MINITPV JAVA STORE         ");
            System.out.println("====================================");
            System.out.println("1. Registrar producto");
            System.out.println("2. Listar productos");
            System.out.println("3. Buscar producto por código");
            System.out.println("4. Realizar venta");
            System.out.println("5. Ver historial de ventas");
            System.out.println("6. Salir");
            System.out.print("Seleccione una opción: ");

            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1": registrarProducto(); break;
                case "2": listarProductos(); break;
                case "3": buscarProducto(); break;
                case "4": realizarVenta(); break;
                case "5": verHistorial(); break;
                case "6": 
                    salir = true; 
                    System.out.println("Saliendo del sistema...");
                    Bdd.getInstance().close();
                    break;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
    }

    // --- 1. REGISTRAR PRODUCTO ---
    private void registrarProducto() {
        System.out.println("\n--- REGISTRO DE PRODUCTO ---");
        
        System.out.print("Código: ");
        String codigo = scanner.nextLine().trim();
        if (codigo.isEmpty() || prodDAO.existeCodigo(codigo)) {
            System.out.println("Error: El código está vacío o ya existe.");
            return;
        }

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine().trim();
        if (nombre.isEmpty()) {
            System.out.println("Error: El nombre no puede estar vacío.");
            return;
        }

        double precio;
        int stock;
        try {
            System.out.print("Precio: ");
            precio = Double.parseDouble(scanner.nextLine().trim());
            if (precio < 0) throw new NumberFormatException();

            System.out.print("Stock inicial: ");
            stock = Integer.parseInt(scanner.nextLine().trim());
            if (stock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Error: Precio y stock deben ser números válidos y positivos.");
            return;
        }

        Productos p = new Productos(0, codigo, nombre, precio, stock);
        if (prodDAO.guardar(p)) {
            System.out.println("\nProducto registrado correctamente.");
        } else {
            System.out.println("\nError al guardar el producto en la base de datos.");
        }
    }

    // --- 2. LISTAR PRODUCTOS ---
    private void listarProductos() {
        System.out.println("\n--- LISTADO DE PRODUCTOS ---");
        List<Productos> lista = prodDAO.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay productos registrados.");
        } else {
            for (Productos p : lista) {
                System.out.println(p.toString());
            }
        }
    }

    // --- 3. BUSCAR PRODUCTO ---
    private void buscarProducto() {
        System.out.print("\nIntroduzca el código del producto a buscar: ");
        String codigo = scanner.nextLine().trim();
        Productos p = prodDAO.buscarPorCodigo(codigo);
        
        if (p != null) {
            System.out.println("Producto encontrado: " + p.toString());
        } else {
            System.out.println("No se encontró ningún producto con ese código.");
        }
    }

    // --- 4. REALIZAR VENTA (Transaccional) ---
    private void realizarVenta() {
        System.out.println("\n--- NUEVA VENTA ---");
        List<ItemCarrito> carrito = new ArrayList<>();
        boolean comprando = true;

        while (comprando) {
            System.out.print("\nCódigo del producto: ");
            String codigo = scanner.nextLine().trim();
            Productos p = prodDAO.buscarPorCodigo(codigo);

            if (p == null) {
                System.out.println("Producto no encontrado.");
            } else {
                System.out.println("Producto: " + p.getNombre());
                System.out.println("Precio unitario: " + String.format("%.2f €", p.getPrecio()));
                System.out.println("Stock disponible: " + p.getStock());

                System.out.print("Cantidad: ");
                try {
                    int cantidad = Integer.parseInt(scanner.nextLine().trim());
                    if (cantidad <= 0) {
                        System.out.println("La cantidad debe ser mayor que 0.");
                    } else if (cantidad > p.getStock()) {
                        System.out.println("No hay stock suficiente.");
                    } else {
                        carrito.add(new ItemCarrito(p, cantidad));
                        // Reducimos el stock temporalmente en memoria para esta venta
                        p.setStock(p.getStock() - cantidad); 
                        System.out.println("\nProducto añadido al carrito.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Cantidad no válida.");
                }
            }

            System.out.print("\n¿Desea añadir otro producto? S/N: ");
            String respuesta = scanner.nextLine().trim().toUpperCase();
            if (!respuesta.equals("S")) {
                comprando = false;
            }
        }

        if (carrito.isEmpty()) {
            System.out.println("Venta cancelada. El carrito está vacío.");
            return;
        }

        // Cálculos
        double subtotal = 0.0;
        for (ItemCarrito item : carrito) {
            subtotal += item.totalLinea;
        }
        double iva = subtotal * 0.21;
        double total = subtotal + iva;

        // Proceso transaccional en Base de Datos
        Connection con = null;
        try {
            con = Bdd.getInstance().getConnection();
            con.setAutoCommit(false); // 1. Iniciar transacción

            // 2. Insertar Venta y obtener el ID generado
            String sqlVenta = "INSERT INTO sales (sale_date, subtotal, tax, total) VALUES (CURRENT_TIMESTAMP, ?, ?, ?)";
            int ventaId = 0;
            try (PreparedStatement stmtVenta = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                stmtVenta.setDouble(1, subtotal);
                stmtVenta.setDouble(2, iva);
                stmtVenta.setDouble(3, total);
                stmtVenta.executeUpdate();

                try (ResultSet rs = stmtVenta.getGeneratedKeys()) {
                    if (rs.next()) {
                        ventaId = rs.getInt(1);
                    } else {
                        throw new SQLException("No se pudo obtener el ID de la venta.");
                    }
                }
            }

            // 3 y 4. Insertar líneas de venta y actualizar stock
            String sqlLinea = "INSERT INTO sale_lines (sale_id, product_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";
            String sqlStock = "UPDATE products SET stock = stock - ? WHERE id = ?";

            try (PreparedStatement stmtLinea = con.prepareStatement(sqlLinea);
                 PreparedStatement stmtStock = con.prepareStatement(sqlStock)) {
                
                for (ItemCarrito item : carrito) {
                    // Línea
                    stmtLinea.setInt(1, ventaId);
                    stmtLinea.setInt(2, item.producto.getId());
                    stmtLinea.setInt(3, item.cantidad);
                    stmtLinea.setDouble(4, item.producto.getPrecio());
                    stmtLinea.setDouble(5, item.totalLinea);
                    stmtLinea.executeUpdate();

                    // Stock
                    stmtStock.setInt(1, item.cantidad);
                    stmtStock.setInt(2, item.producto.getId());
                    stmtStock.executeUpdate();
                }
            }

            con.commit(); // 5. Confirmar transacción
            generarTicket(ventaId, carrito, subtotal, iva, total); // 6. Generar ticket

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback(); // Deshacer si hay error
                    System.out.println("Error durante la venta. Operación cancelada (Rollback).");
                } catch (SQLException ex) {
                    System.out.println("Error fatal al hacer rollback: " + ex.getMessage());
                }
            }
            System.out.println("Detalle del error: " + e.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true); // Restaurar estado por defecto
                } catch (SQLException e) {
                    System.out.println("Error restaurando autocommit: " + e.getMessage());
                }
            }
        }
    }

    // --- GENERAR TICKET (Pantalla y .txt) ---
    private void generarTicket(int ventaId, List<ItemCarrito> carrito, double subtotal, double iva, double total) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String fecha = sdf.format(new Date());

        StringBuilder ticket = new StringBuilder();
        ticket.append("\n====================================\n");
        ticket.append("           TICKET DE COMPRA         \n");
        ticket.append("====================================\n");
        ticket.append("Venta Nº: ").append(ventaId).append("\n");
        ticket.append("Fecha: ").append(fecha).append("\n\n");
        ticket.append(String.format("%-20s %-7s %s\n", "Producto", "Cant.", "Total"));
        ticket.append("------------------------------------\n");
        
        for (ItemCarrito item : carrito) {
            ticket.append(String.format("%-20s %-7d %.2f\n", 
                recortarNombre(item.producto.getNombre(), 19), 
                item.cantidad, 
                item.totalLinea));
        }
        
        ticket.append("------------------------------------\n");
        ticket.append(String.format("%-25s %7.2f €\n", "Subtotal:", subtotal));
        ticket.append(String.format("%-25s %7.2f €\n", "IVA 21%:", iva));
        ticket.append(String.format("%-25s %7.2f €\n", "TOTAL:", total));
        ticket.append("\nGracias por su compra.\n");
        ticket.append("====================================\n");

        // Imprimir por pantalla
        System.out.println(ticket.toString());

        // Guardar en TXT
        guardarTicketTxt(ventaId, ticket.toString());
    }

    // Auxiliar para que el ticket no se descuadre si el nombre es muy largo
    private String recortarNombre(String nombre, int max) {
        if (nombre.length() > max) {
            return nombre.substring(0, max);
        }
        return nombre;
    }

    private void guardarTicketTxt(int ventaId, String contenido) {
        File directorio = new File("tickets");
        if (!directorio.exists()) {
            directorio.mkdir(); // Crea la carpeta si no existe
        }

        String nombreArchivo = "tickets/ticket-venta-" + String.format("%03d", ventaId) + ".txt";
        try (FileWriter writer = new FileWriter(nombreArchivo)) {
            writer.write(contenido);
            System.out.println("(Ticket guardado en: " + nombreArchivo + ")");
        } catch (IOException e) {
            System.out.println("No se pudo guardar el ticket en archivo: " + e.getMessage());
        }
    }

    // --- 5. VER HISTORIAL ---
    private void verHistorial() {
        System.out.println("\n--- HISTORIAL DE VENTAS ---");
        List<Ventas> lista = ventDAO.listarTodas();
        if (lista.isEmpty()) {
            System.out.println("No hay ventas registradas.");
        } else {
            for (Ventas v : lista) {
                System.out.println(v.toString());
            }
        }
    }

    // Método main para probar solo la interfaz
    public static void main(String[] args) {
        Menu menu = new Menu();
        menu.iniciar();
    }
}
