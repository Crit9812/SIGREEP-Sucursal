package Compartido.Metodos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class validacionEstadosDeDetalleEntrada {

    public static void ejecutar(Connection connOrigen, int idEntrada) throws SQLException {

        String sql = """
            SELECT estado, COUNT(*) AS total
            FROM detalle_Entrada
            WHERE claveEntrada = ?
            GROUP BY estado
        """;

        int disponibles = 0;
        int pendientes = 0;
        int vendidos = 0;
        int traspasados = 0;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {

            ps.setInt(1, idEntrada);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String estado = rs.getString("estado");
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

        actualizarEstadoEntrada(connOrigen, idEntrada, nuevoEstado);
    }

    private static void actualizarEstadoEntrada(Connection connOrigen,
                                                int idEntrada,
                                                String nuevoEstado) throws SQLException {

        String sql = """
            UPDATE entradas
            SET Estado = ?
            WHERE idEntrada = ?
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idEntrada);
            ps.executeUpdate();
        }
    }
}