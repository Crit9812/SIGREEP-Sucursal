package Compartido.Metodos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class actualizarEstadoArticulo {

    public static void ejecutar(Connection connOrigen, String idArticulo, String nuevoEstado) throws SQLException {
        String sql = "UPDATE articulo SET Estado = ? WHERE idArticulo = ?";

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, idArticulo);
            ps.executeUpdate();
        }
    }
}