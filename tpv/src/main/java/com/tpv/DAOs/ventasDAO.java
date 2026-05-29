package com.tpv.DAOs;

import com.tpv.conectarbdd.Bdd;
import com.tpv.modelos.Ventas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentasDAO {

    // ── GUARDAR una venta nueva ───────────────────────────────
    public boolean guardar(Ventas v) {

        String sql = "INSERT INTO sales (subtotal, tax, total) "
                   + "VALUES (?, ?, ?)";

        try (Connection con       = Bdd.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setDouble(1, v.getSubtotal());
            stmt.setDouble(2, v.getTax());
            stmt.setDouble(3, v.getTotal());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al guardar venta: " + e.getMessage());
            return false;
        }
    }

    // ── LISTAR todas las ventas ───────────────────────────────
    public List<Ventas> listarTodas() {

        List<Ventas> lista = new ArrayList<>();
        String sql = "SELECT id, sale_date, subtotal, tax, total "
                   + "FROM sales ORDER BY sale_date DESC";

        try (Connection con = Bdd.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar ventas: " + e.getMessage());
        }

        return lista;
    }

    // ── BUSCAR por id ─────────────────────────────────────────
    public Ventas buscarPorId(int id) {

        String sql = "SELECT id, sale_date, subtotal, tax, total "
                   + "FROM sales WHERE id = ?";

        try (Connection con       = Bdd.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearFila(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar venta: " + e.getMessage());
        }

        return null; // No encontrada
    }

    // ── COMPROBAR si ya existe una venta con ese id ───────────
    public boolean existeId(int id) {
        return buscarPorId(id) != null;
    }

    // ── Convierte una fila SQL en un objeto Ventas ────────────
    private Ventas mapearFila(ResultSet rs) throws SQLException {
        return new Ventas(
            rs.getInt      ("id"),
            rs.getTimestamp("sale_date"),
            rs.getDouble   ("subtotal"),
            rs.getDouble   ("tax"),
            rs.getDouble   ("total")
        );
    }
}
