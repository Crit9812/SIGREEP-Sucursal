package Compartido.Metodos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class eliminarArticulosSalidaNormal {

    public static void ejecutar(Connection connOrigen,
                                int idSalida,
                                int idDetalleSalida) throws SQLException {

        String sql = """
            SELECT idArticulo
            FROM articulo
            WHERE idDetalleSalida = ?
            ORDER BY idArticulo
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idDetalleSalida);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idArticulo = rs.getInt("idArticulo");
                    eliminarArticuloSalida.ejecutar(connOrigen, idSalida, idDetalleSalida, idArticulo);
                }
            }
        }
    }
}