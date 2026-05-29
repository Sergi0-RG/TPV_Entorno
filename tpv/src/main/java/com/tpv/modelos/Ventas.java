package com.tpv.modelos;

import java.util.Date;

public class Ventas {

    private int    id;
    private Date   fecha;
    private double subtotal;
    private double tax;
    private double total;

    // Constructor vacío (necesario para crear objetos en blanco)
    public Ventas() {}

    // Constructor completo (para rellenar desde la base de datos)
    public Ventas(int id, Date fecha, double subtotal, double tax, double total) {
        this.id       = id;
        this.fecha    = fecha;
        this.subtotal = subtotal;
        this.tax      = tax;
        this.total    = total;
    }

    // Getters y Setters
    public int    getId()                         { return id; }
    public void   setId(int id)                   { this.id = id; }

    public Date   getFecha()                      { return fecha; }
    public void   setFecha(Date fecha)            { this.fecha = fecha; }

    public double getSubtotal()                   { return subtotal; }
    public void   setSubtotal(double subtotal)    { this.subtotal = subtotal; }

    public double getTax()                        { return tax; }
    public void   setTax(double tax)              { this.tax = tax; }

    public double getTotal()                      { return total; }
    public void   setTotal(double total)          { this.total = total; }

    // Útil para depurar con System.out.println(venta)
    @Override
    public String toString() {
        return String.format("Venta #%d - Subtotal: %.2f€  IVA: %.2f€  Total: %.2f€",
                             id, subtotal, tax, total);
    }
}
