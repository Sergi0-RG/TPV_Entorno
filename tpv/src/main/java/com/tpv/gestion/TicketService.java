package com.tpv.gestion;

import com.tpv.modelos.Productos;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Genera un fichero .txt con el ticket de la venta.
 * Los tickets se guardan en la carpeta "tickets/" del directorio
 * de trabajo con el nombre  ticket_YYYYMMDD_HHmmss.txt
 */
public class TicketService {

    // ─────────────────────────────────────────────────────────────
    //  Tipo auxiliar que representa una línea de la venta.
    //  Main.java crea objetos de este tipo en lugar de usar
    //  su propio record, así TicketService puede leerlos sin
    //  depender de Main.
    // ─────────────────────────────────────────────────────────────
    public static class LineaVenta {

        private final Productos producto;
        private final int       cantidad;

        public LineaVenta(Productos producto, int cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }

        public Productos getProducto() { return producto; }
        public int       getCantidad() { return cantidad; }
        public double    getSubtotal() { return producto.getPrecio() * cantidad; }
    }

    // ─────────────────────────────────────────────────────────────
    //  Genera el ticket y devuelve la ruta del fichero creado.
    // ─────────────────────────────────────────────────────────────
    public String generarTicket(List<LineaVenta> lineas,
                                double entregado,
                                double cambio) {

        LocalDateTime ahora = LocalDateTime.now();
        String ts      = ahora.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String carpeta = "tickets";
        String ruta    = carpeta + "/ticket_" + ts + ".txt";

        new java.io.File(carpeta).mkdirs();

        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta))) {

            final String DOBLE = "═".repeat(40);
            final String FINA  = "─".repeat(40);

            // ── Cabecera ───────────────────────────────────────
            pw.println(DOBLE);
            pw.println(centrar("MINI TPV", 40));
            pw.println(centrar("C/ Ejemplo, 1  ·  Málaga", 40));
            pw.println(centrar("Tel: 951 000 000", 40));
            pw.println(FINA);
            pw.printf("  Fecha : %s%n",
                    ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss")));
            pw.printf("  Ticket: %s%n", ts);
            pw.println(FINA);

            // ── Cabecera de columnas ───────────────────────────
            pw.printf("  %-18s %5s %7s %8s%n",
                    "PRODUCTO", "CANT.", "PRECIO", "IMPORTE");
            pw.println(FINA);

            // ── Líneas de productos ────────────────────────────
            double total = 0;
            for (LineaVenta l : lineas) {
                String nombre   = l.getProducto().getNombre();
                int    cant     = l.getCantidad();
                double precio   = l.getProducto().getPrecio();
                double subtotal = l.getSubtotal();
                total += subtotal;

                if (nombre.length() > 18) nombre = nombre.substring(0, 17) + ".";

                pw.printf("  %-18s %5d %6.2f€ %7.2f€%n",
                        nombre, cant, precio, subtotal);
            }

            // ── Totales con desglose de IVA ───────────────────
            pw.println(FINA);
            double baseImponible = total / 1.21;
            double iva           = total - baseImponible;

            pw.printf("  %-28s %7.2f€%n", "Base imponible:", baseImponible);
            pw.printf("  %-28s %7.2f€%n", "IVA (21%):", iva);
            pw.println(FINA);
            pw.printf("  %-28s %7.2f€%n", "TOTAL:", total);
            pw.println(FINA);
            pw.printf("  %-28s %7.2f€%n", "Entregado:", entregado);
            pw.printf("  %-28s %7.2f€%n", "Cambio:", cambio);

            // ── Pie ────────────────────────────────────────────
            pw.println(DOBLE);
            pw.println(centrar("¡Gracias por su compra!", 40));
            pw.println(centrar("Conserve el ticket.", 40));
            pw.println(DOBLE);

        } catch (IOException e) {
            System.err.println("[Ticket] Error al escribir: " + e.getMessage());
        }

        return ruta;
    }

    // ── Centra un texto en un ancho fijo ───────────────────────
    private String centrar(String texto, int ancho) {
        int pad = Math.max(0, (ancho - texto.length()) / 2);
        return " ".repeat(pad) + texto;
    }
}