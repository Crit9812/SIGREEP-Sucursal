package Compartido.Metodos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class obtenerTotales {

    public static void ejecutar(Connection connOrigen, int idSalida) throws SQLException {
        String sql = """
            SELECT precioBrutoTotalSalida, precioTotalSalida
            FROM detalle_Salida
            WHERE claveSalida = ?
        """;

        BigDecimal precioNeto = BigDecimal.ZERO;
        BigDecimal precioTotal = BigDecimal.ZERO;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idSalida);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    precioNeto = precioNeto.add(obtenerBigDecimal(rs, "precioBrutoTotalSalida"));
                    precioTotal = precioTotal.add(obtenerBigDecimal(rs, "precioTotalSalida"));
                }
            }
        }

        String update = """
            UPDATE salidas
            SET precioNetoSalida = ?,
                precioTotalSalida = ?
            WHERE idSalida = ?
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(update)) {
            ps.setBigDecimal(1, precioNeto);
            ps.setBigDecimal(2, precioTotal);
            ps.setInt(3, idSalida);
            ps.executeUpdate();
        }
    }

    private static BigDecimal obtenerBigDecimal(ResultSet rs, String columna) throws SQLException {
        BigDecimal valor = rs.getBigDecimal(columna);
        return valor != null ? valor : BigDecimal.ZERO;
    }
}