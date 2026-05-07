package Compartido.Metodos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class actualizarDetalleSalidaARechazado {

    public static void ejecutar(Connection connOrigen, int idDetalleSalida) throws SQLException {
        String sql = """
            UPDATE detalle_Salida
            SET estado = ?
            WHERE idDetalleSalida = ?
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setString(1, "rechazado");
            ps.setInt(2, idDetalleSalida);
            ps.executeUpdate();
        }
    }
}