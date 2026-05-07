package Compartido.Metodos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class actualizarMovimientosParaSalidas {

    public static void ejecutar(Connection connOrigen,
                                int idSalida,
                                int idDetalleSalida) throws SQLException {

        String sql = """
            UPDATE detalle_Salida
            SET cantidad = CASE
                WHEN cantidad > 0 THEN cantidad - 1
                ELSE 0
            END
            WHERE idDetalleSalida = ?
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idDetalleSalida);
            ps.executeUpdate();
        }

        calcularCostoTotalDelDetalle.ejecutar(connOrigen, idDetalleSalida);
        obtenerTotales.ejecutar(connOrigen, idSalida);
    }
}