package Compartido.Metodos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class habilitarEntradas {

    public static void ejecutar(Connection connOrigen, int idArticulo) throws SQLException {
        Integer idDetalleEntrada = obtenerIdDetalleEntradaDelArticulo(connOrigen, idArticulo);

        if (idDetalleEntrada == null || idDetalleEntrada <= 0) {
            return;
        }

        String updateDetalle = """
            UPDATE detalle_Entrada
            SET estado = ?
            WHERE idDetalleEntrada = ?
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(updateDetalle)) {
            ps.setString(1, "disponible");
            ps.setInt(2, idDetalleEntrada);
            ps.executeUpdate();
        }

        Integer idEntrada = obtenerIdEntradaDesdeDetalleEntrada(connOrigen, idDetalleEntrada);

        if (idEntrada == null || idEntrada <= 0) {
            return;
        }

        String updateEntrada = """
            UPDATE entradas
            SET Estado = ?
            WHERE idEntrada = ?
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(updateEntrada)) {
            ps.setString(1, "disponible");
            ps.setInt(2, idEntrada);
            ps.executeUpdate();
        }
    }

    private static Integer obtenerIdDetalleEntradaDelArticulo(Connection connOrigen, int idArticulo) throws SQLException {
        String sql = """
            SELECT idDetalleEntrada
            FROM articulo
            WHERE idArticulo = ?
            LIMIT 1
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idArticulo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Object valor = rs.getObject("idDetalleEntrada");
                    return valor != null ? rs.getInt("idDetalleEntrada") : null;
                }
            }
        }

        return null;
    }

    private static Integer obtenerIdEntradaDesdeDetalleEntrada(Connection connOrigen, int idDetalleEntrada) throws SQLException {
        String sql = """
            SELECT claveEntrada
            FROM detalle_Entrada
            WHERE idDetalleEntrada = ?
            LIMIT 1
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idDetalleEntrada);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Object valor = rs.getObject("claveEntrada");
                    return valor != null ? rs.getInt("claveEntrada") : null;
                }
            }
        }

        return null;
    }
}