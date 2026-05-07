package Compartido.Metodos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class eliminarArticuloYSusDetallesDeArticulo {

    public static void ejecutar(Connection connOrigen,
                                int idSalida,
                                int idDetalleSalida,
                                String idArticulo,
                                List<String> detallesArticulo) throws SQLException {

        System.out.println("Si entra aqui esta bien pero mal");

        for (String idDetalleArticulo : detallesArticulo) {
            String sql = """
                UPDATE detalleArticulo
                SET idDetalleSalida = NULL,
                    estado = ?
                WHERE idDetalle = ?
            """;

            try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
                ps.setString(1, "disponible");
                ps.setString(2, idDetalleArticulo);
                ps.executeUpdate();
            }

            actualizarMovimientosParaSalidas.ejecutar(connOrigen, idSalida, idDetalleSalida);
        }

        actualizarEstadoArticulo.ejecutar(connOrigen, idArticulo, "disponible");
        habilitarEntradas.ejecutar(connOrigen, Integer.parseInt(idArticulo));
    }
}