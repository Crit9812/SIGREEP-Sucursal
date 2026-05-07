package Compartido.Metodos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class calcularCostoTotalDelDetalle {

    public static void ejecutar(Connection connOrigen, int idDetalleSalida) throws SQLException {
        String sql = """
            SELECT cantidad, precioUnitarioSalida, precioIVASalida
            FROM detalle_Salida
            WHERE idDetalleSalida = ?
            LIMIT 1
        """;

        int cantidad = 0;
        BigDecimal precioUnitario = BigDecimal.ZERO;
        BigDecimal precioIva = BigDecimal.ZERO;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idDetalleSalida);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cantidad = rs.getInt("cantidad");
                    precioUnitario = obtenerBigDecimal(rs, "precioUnitarioSalida");
                    precioIva = obtenerBigDecimal(rs, "precioIVASalida");
                }
            }
        }

        BigDecimal precioBrutoTotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        BigDecimal precioTotal = precioIva.multiply(BigDecimal.valueOf(cantidad));

        String update = """
            UPDATE detalle_Salida
            SET precioBrutoTotalSalida = ?,
                precioTotalSalida = ?
            WHERE idDetalleSalida = ?
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(update)) {
            ps.setBigDecimal(1, precioBrutoTotal);
            ps.setBigDecimal(2, precioTotal);
            ps.setInt(3, idDetalleSalida);
            ps.executeUpdate();
        }
    }

    private static BigDecimal obtenerBigDecimal(ResultSet rs, String columna) throws SQLException {
        BigDecimal valor = rs.getBigDecimal(columna);
        return valor != null ? valor : BigDecimal.ZERO;
    }
}