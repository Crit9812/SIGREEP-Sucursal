package Compartido.Metodos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class eliminarArticuloSalida {

    public static void ejecutar(Connection connOrigen,
                                int idSalida,
                                int idDetalleSalida,
                                int idArticulo) throws SQLException {

        String sql = """
            UPDATE articulo
            SET idDetalleSalida = NULL,
                Estado = ?
            WHERE idArticulo = ?
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setString(1, "disponible");
            ps.setInt(2, idArticulo);
            ps.executeUpdate();
        }

        actualizarMovimientosParaSalidas.ejecutar(connOrigen, idSalida, idDetalleSalida);
        habilitarEntradas.ejecutar(connOrigen, idArticulo);
    }
}