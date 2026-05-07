package Compartido.Metodos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class validacionEstadosDeArticulo {

    public static void ejecutar(Connection connOrigen, int idDetalleEntrada) throws SQLException {

        String sql = """
            SELECT Estado, COUNT(*) AS total
            FROM articulo
            WHERE idDetalleEntrada = ?
            GROUP BY Estado
        """;

        int disponibles = 0;
        int pendientes = 0;
        int vendidos = 0;
        int traspasados = 0;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {

            ps.setInt(1, idDetalleEntrada);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String estado = rs.getString("Estado");
                    int total = rs.getInt("total");

                    if (estado == null) {
                        continue;
                    }

                    switch (estado.toLowerCase()) {

                        case "disponible":
                            disponibles = total;
                            break;

                        case "pendiente":
                            pendientes = total;
                            break;

                        case "vendido":
                            vendidos = total;
                            break;

                        case "traspasado":
                            traspasados = total;
                            break;
                    }
                }
            }
        }

        String nuevoEstado;

        if (disponibles > 0) {
            nuevoEstado = "disponible";
        } else if (pendientes > 0) {
            nuevoEstado = "pendiente";
        } else if (vendidos > 0) {
            nuevoEstado = "vendido";
        } else if (traspasados > 0) {
            nuevoEstado = "traspasado";
        } else {
            nuevoEstado = "cancelado";
        }

        actualizarEstadoDetalleEntrada(connOrigen, idDetalleEntrada, nuevoEstado);
    }

    private static void actualizarEstadoDetalleEntrada(Connection connOrigen,
                                                       int idDetalleEntrada,
                                                       String nuevoEstado) throws SQLException {

        String sql = """
            UPDATE detalle_Entrada
            SET estado = ?
            WHERE idDetalleEntrada = ?
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idDetalleEntrada);
            ps.executeUpdate();
        }
    }
}