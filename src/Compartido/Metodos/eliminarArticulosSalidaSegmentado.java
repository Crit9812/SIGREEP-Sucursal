package Compartido.Metodos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class eliminarArticulosSalidaSegmentado {

    public static void ejecutar(Connection connOrigen,
                                int idSalida,
                                int idDetalleSalida) throws SQLException {

        Map<String, List<String>> detallesAgrupados = new LinkedHashMap<>();

        String sql = """
            SELECT idDetalle, idArticulo
            FROM detalleArticulo
            WHERE idDetalleSalida = ?
            ORDER BY idArticulo, idDetalle
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idDetalleSalida);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String idDetalle = rs.getString("idDetalle");
                    String idArticulo = rs.getString("idArticulo");

                    if (idArticulo == null || idArticulo.isBlank()) {
                        continue;
                    }

                    detallesAgrupados
                            .computeIfAbsent(idArticulo, k -> new ArrayList<>())
                            .add(idDetalle);
                }
            }
        }

        for (Map.Entry<String, List<String>> grupo : detallesAgrupados.entrySet()) {
            eliminarArticuloYSusDetallesDeArticulo.ejecutar(
                    connOrigen,
                    idSalida,
                    idDetalleSalida,
                    grupo.getKey(),
                    grupo.getValue()
            );
        }
    }
}