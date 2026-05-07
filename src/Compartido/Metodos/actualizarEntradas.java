package Compartido.Metodos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class actualizarEntradas {

    public static void ejecutar(Connection connOrigen, int idSalida) throws SQLException {
        String sql = """
            SELECT idDetalleSalida, tipoDetalle
            FROM detalle_Salida
            WHERE claveSalida = ?
            ORDER BY idDetalleSalida
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idSalida);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idDetalleSalida = rs.getInt("idDetalleSalida");
                    int tipoDetalle = rs.getInt("tipoDetalle");

                    if (tipoDetalle == 0) {
                        procesarArticulosDetalleSalidaOrigen(connOrigen, idDetalleSalida);
                    }
                }
            }
        }
    }

    private static void procesarArticulosDetalleSalidaOrigen(Connection connOrigen, int idDetalleSalida) throws SQLException {
        String sql = """
            SELECT idArticulo, idDetalleEntrada
            FROM articulo
            WHERE idDetalleSalida = ?
            ORDER BY idArticulo
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idDetalleSalida);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idArticulo = rs.getInt("idArticulo");
                    int idDetalleEntrada = rs.getInt("idDetalleEntrada");

                    if (idArticulo <= 0 || idDetalleEntrada <= 0) {
                        continue;
                    }

                    Integer idEntrada = obtenerEntradaPorDetalleEntrada(connOrigen, idDetalleEntrada);

                    if (idEntrada == null || idEntrada <= 0) {
                        continue;
                    }

                    validacionEstadosDeArticulo.ejecutar(connOrigen, idDetalleEntrada);
                    validacionEstadosDeDetalleEntrada.ejecutar(connOrigen, idEntrada);
                }
            }
        }
    }

    private static Integer obtenerEntradaPorDetalleEntrada(Connection connOrigen, int idDetalleEntrada) throws SQLException {
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
                    return rs.getInt("claveEntrada");
                }
            }
        }

        return null;
    }
}