package com.tpv.DAOs;

import com.tpv.conectarbdd.bdd;
import com.tpv.modelos.Productos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class productosDAO {

    // ── GUARDAR un producto nuevo ─────────────────────────────
    public boolean guardar(Productos p) {

        String sql = "INSERT INTO products (code, name, price, stock) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection con  = Bdd.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, p.getCodigo());
            stmt.setString(2, p.getNombre());
            stmt.setDouble(3, p.getPrecio());
            stmt.setInt   (4, p.getStock());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al guardar producto: " + e.getMessage());
            return false;
        }
    }

    // ── LISTAR todos los productos ────────────────────────────
    public List<Productos> listarTodos() {

        List<Productos> lista = new ArrayList<>();
        String sql = "SELECT id, code, name, price, stock "
                   + "FROM products ORDER BY name";

        try (Connection con  = Bdd.getInstance().getConnection();
             Statement stmt  = con.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar productos: " + e.getMessage());
        }

        return lista;
    }

    // ── BUSCAR por código ─────────────────────────────────────
    public Productos buscarPorCodigo(String codigo) {

        String sql = "SELECT id, code, name, price, stock "
                   + "FROM products WHERE code = ?";

        try (Connection con  = Bdd.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, codigo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearFila(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar producto: " + e.getMessage());
        }

        return null; // No encontrado
    }

    // ── COMPROBAR si ya existe un código ──────────────────────
    public boolean existeCodigo(String codigo) {
        return buscarPorCodigo(codigo) != null;
    }

    // ── Convierte una fila SQL en un objeto Productos ─────────
    private Productos mapearFila(ResultSet rs) throws SQLException {
        return new Productos(
            rs.getInt   ("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getDouble("price"),
            rs.getInt   ("stock")
        );
    }
}