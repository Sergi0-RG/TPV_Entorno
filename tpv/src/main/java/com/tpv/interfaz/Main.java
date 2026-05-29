package com.tpv.interfaz;

import com.tpv.DAOs.productosDAO;
import com.tpv.modelos.Productos;
import com.tpv.gestion.TicketService;
import com.tpv.gestion.TicketService.LineaVenta;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        productosDAO     dao    = new productosDAO();
        TicketService    ticket = new TicketService();
        List<LineaVenta> venta  = new ArrayList<>();
        Scanner          sc     = new Scanner(System.in);

        System.out.println("╔══════════════════════════════╗");
        System.out.println("║       MINI TPV  v1.0         ║");
        System.out.println("╚══════════════════════════════╝");

        boolean ejecutando = true;
        while (ejecutando) {
            mostrarMenu();
            String opcion = sc.nextLine().trim();

            switch (opcion) {

                // ── 1. Añadir producto a la venta ──────────────
                case "1" -> {
                    System.out.print("  Código del producto: ");
                    String codigo = sc.nextLine().trim().toUpperCase();

                    Productos p = dao.buscarPorCodigo(codigo);
                    if (p == null) {
                        System.out.println("  ✗ Producto no encontrado.");
                        break;
                    }

                    System.out.print("  Cantidad: ");
                    int cant = leerEntero(sc);
                    if (cant <= 0) {
                        System.out.println("  ✗ Cantidad inválida.");
                        break;
                    }
                    if (cant > p.getStock()) {
                        System.out.printf("  ✗ Stock insuficiente (disponible: %d)%n",
                                p.getStock());
                        break;
                    }

                    // Si el producto ya estaba en la venta, actualizamos cantidad
                    boolean yaExiste = false;
                    for (int i = 0; i < venta.size(); i++) {
                        if (venta.get(i).getProducto().getCodigo().equals(codigo)) {
                            int nueva = venta.get(i).getCantidad() + cant;
                            venta.set(i, new LineaVenta(p, nueva));
                            yaExiste = true;
                            break;
                        }
                    }
                    if (!yaExiste) {
                        venta.add(new LineaVenta(p, cant));
                    }

                    System.out.printf("  ✔ %s × %d añadido%n", p.getNombre(), cant);
                }

                // ── 2. Ver venta actual ────────────────────────
                case "2" -> mostrarVenta(venta);

                // ── 3. Eliminar una línea ──────────────────────
                case "3" -> {
                    if (venta.isEmpty()) {
                        System.out.println("  La venta está vacía.");
                        break;
                    }
                    mostrarVenta(venta);
                    System.out.print("  Nº de línea a eliminar: ");
                    int nLinea = leerEntero(sc) - 1;
                    if (nLinea < 0 || nLinea >= venta.size()) {
                        System.out.println("  ✗ Número de línea inválido.");
                    } else {
                        System.out.printf("  ✔ Eliminado: %s%n",
                                venta.get(nLinea).getProducto().getNombre());
                        venta.remove(nLinea);
                    }
                }

                // ── 4. Cobrar y generar ticket ─────────────────
                case "4" -> {
                    if (venta.isEmpty()) {
                        System.out.println("  ✗ La venta está vacía.");
                        break;
                    }
                    mostrarVenta(venta);

                    double total = venta.stream()
                            .mapToDouble(LineaVenta::getSubtotal).sum();

                    System.out.printf("%n  TOTAL: %.2f €%n", total);
                    System.out.print("  Importe entregado: ");
                    double entregado = leerDecimal(sc);

                    if (entregado < total) {
                        System.out.println("  ✗ Importe insuficiente.");
                        break;
                    }

                    double cambio = entregado - total;
                    System.out.printf("  ✔ Cambio: %.2f €%n", cambio);

                    String ruta = ticket.generarTicket(venta, entregado, cambio);
                    System.out.println("  ✔ Ticket guardado en: " + ruta);

                    venta.clear(); // Nueva venta
                }

                // ── 5. Dar de alta un producto nuevo ───────────
                case "5" -> {
                    System.out.print("  Código (ej. PROD001): ");
                    String cod = sc.nextLine().trim().toUpperCase();

                    if (dao.existeCodigo(cod)) {
                        System.out.println("  ✗ Ese código ya existe.");
                        break;
                    }

                    System.out.print("  Nombre: ");
                    String nombre = sc.nextLine().trim();

                    System.out.print("  Precio (€): ");
                    double precio = leerDecimal(sc);

                    System.out.print("  Stock inicial: ");
                    int stock = leerEntero(sc);

                    Productos nuevo = new Productos(0, cod, nombre, precio, stock);
                    if (dao.guardar(nuevo)) {
                        System.out.println("  ✔ Producto dado de alta.");
                    } else {
                        System.out.println("  ✗ Error al guardar en la base de datos.");
                    }
                }

                // ── 6. Listar catálogo completo ────────────────
                case "6" -> {
                    List<Productos> todos = dao.listarTodos();
                    if (todos.isEmpty()) {
                        System.out.println("  No hay productos registrados.");
                    } else {
                        System.out.println();
                        System.out.printf("  %-10s %-25s %8s %6s%n",
                                "CÓDIGO", "NOMBRE", "PRECIO", "STOCK");
                        System.out.println("  " + "─".repeat(55));
                        for (Productos p : todos) {
                            System.out.printf("  %-10s %-25s %7.2f€ %6d%n",
                                    p.getCodigo(), p.getNombre(),
                                    p.getPrecio(), p.getStock());
                        }
                        System.out.println();
                    }
                }

                // ── 0. Salir ───────────────────────────────────
                case "0" -> {
                    System.out.println("  Hasta luego!");
                    ejecutando = false;
                }

                default -> System.out.println("  ✗ Opción no válida.");
            }
        }

        sc.close();
    }

    // ── Menú principal ─────────────────────────────────────────
    private static void mostrarMenu() {
        System.out.println();
        System.out.println("  ┌──────────────────────────┐");
        System.out.println("  │  1. Añadir producto       │");
        System.out.println("  │  2. Ver venta actual      │");
        System.out.println("  │  3. Eliminar línea        │");
        System.out.println("  │  4. Cobrar / ticket       │");
        System.out.println("  │  5. Dar de alta producto  │");
        System.out.println("  │  6. Listar catálogo       │");
        System.out.println("  │  0. Salir                 │");
        System.out.println("  └──────────────────────────┘");
        System.out.print("  Opción: ");
    }

    // ── Tabla de la venta actual ───────────────────────────────
    private static void mostrarVenta(List<LineaVenta> venta) {
        if (venta.isEmpty()) {
            System.out.println("  La venta está vacía.");
            return;
        }
        System.out.println();
        System.out.printf("  %-4s %-20s %5s %8s %10s%n",
                "Nº", "PRODUCTO", "CANT.", "P.UNIT.", "SUBTOTAL");
        System.out.println("  " + "─".repeat(52));
        for (int i = 0; i < venta.size(); i++) {
            LineaVenta l = venta.get(i);
            System.out.printf("  %-4d %-20s %5d %7.2f€ %9.2f€%n",
                    i + 1,
                    l.getProducto().getNombre(),
                    l.getCantidad(),
                    l.getProducto().getPrecio(),
                    l.getSubtotal());
        }
        System.out.println("  " + "─".repeat(52));
        double total = venta.stream().mapToDouble(LineaVenta::getSubtotal).sum();
        System.out.printf("  %-32s %9.2f€%n", "TOTAL:", total);
        System.out.println();
    }

    // ── Lectura segura de entero ───────────────────────────────
    private static int leerEntero(Scanner sc) {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ── Lectura segura de decimal (acepta coma o punto) ────────
    private static double leerDecimal(Scanner sc) {
        try {
            return Double.parseDouble(sc.nextLine().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}