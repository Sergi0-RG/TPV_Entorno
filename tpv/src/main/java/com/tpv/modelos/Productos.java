package com.tpv.modelos;

public class Productos {
    private int    id;
    private String codigo;
    private String nombre;
    private double precio;
    private int    stock;

    // Constructor vacío (necesario para crear objetos en blanco)
    public Productos() {}

    // Constructor completo (para rellenar desde la base de datos)
    public Productos(int id, String codigo, String nombre,
                     double precio, int stock) {
        this.id          = id;
        this.codigo      = codigo;
        this.nombre      = nombre;
        this.precio      = precio;
        this.stock       = stock;
    }

    // Getters y Setters
    public int    getId()                       { return id; }
    public void   setId(int id)                 { this.id = id; }

    public String getCodigo()                   { return codigo; }
    public void   setCodigo(String codigo)      { this.codigo = codigo; }

    public String getNombre()                   { return nombre; }
    public void   setNombre(String nombre)      { this.nombre = nombre; }

    public double getPrecio()                   { return precio; }
    public void   setPrecio(double precio)      { this.precio = precio; }

    public int    getStock()                    { return stock; }
    public void   setStock(int stock)           { this.stock = stock; }

    // Útil para depurar con System.out.println(producto)
    @Override
    public String toString() {
        return String.format("[%s] %s - %.2f€ (stock: %d)",
                             codigo, nombre, precio, stock);
    }
}
