package Compartido.Metodos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class validacionDeEstadoDeSegmentado {

    public static void ejecutar(Connection connOrigen, String idArticulo) throws SQLException {

        String sql = """
            SELECT estado, COUNT(*) total
            FROM detalleArticulo
            WHERE idArticulo = ?
            GROUP BY estado
        """;

        int disponibles = 0;
        int pendientes = 0;
        int vendidos = 0;
        int traspasados = 0;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {

            ps.setString(1, idArticulo);

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

        if (disponibles > 1) {
            nuevoEstado = "disponible";
        } else if (pendientes > 1) {
            nuevoEstado = "pendiente";
        } else if (vendidos > 1) {
            nuevoEstado = "vendido";
        } else if (traspasados > 1) {
            nuevoEstado = "traspasado";
        } else {
            nuevoEstado = "eliminado";
        }

        actualizarEstadoArticulo.ejecutar(connOrigen, idArticulo, nuevoEstado);
    }
}