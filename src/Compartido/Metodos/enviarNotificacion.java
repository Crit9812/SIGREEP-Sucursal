package Compartido.Metodos;

import conexion.ConexionExtra;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class enviarNotificacion {

    public static void ejecutar(String nombreSucursalOrigen,
                                String nombreSucursalDestino,
                                int idSalidaRemitente,
                                String tipoMovimiento) throws SQLException {

        try (Connection connOrigen = new ConexionExtra().conectar(nombreSucursalOrigen)) {

            String descripcionProductos = obtenerDescripcionTraspaso(connOrigen, idSalidaRemitente);
            String fechaTraspaso = obtenerFechaSalida(connOrigen, idSalidaRemitente);

            boolean esRechazado = "rechazado".equalsIgnoreCase(tipoMovimiento);

            // 🔹 Texto corto (vista general)
            String descripcionCorta;

            // 🔹 Texto detallado (cuando se abre)
            String descripcionDetallada;

            if (esRechazado) {

                descripcionCorta = "Traspaso rechazado de la sucursal " + nombreSucursalDestino;

                descripcionDetallada = "El traspaso realizado el día " + fechaTraspaso +
                        " hacia la sucursal " + nombreSucursalDestino +
                        " fue RECHAZADO.\n\nProductos involucrados: " +
                        descripcionProductos;

            } else {

                descripcionCorta = "Traspaso aceptado en la sucursal " + nombreSucursalDestino;

                descripcionDetallada = "El traspaso realizado el día " + fechaTraspaso +
                        " hacia la sucursal " + nombreSucursalDestino +
                        " fue ACEPTADO.\n\nProductos traspasados: " +
                        descripcionProductos;
            }

            // 🔥 Aquí puedes decidir cuál guardar
            String descripcionFinal = descripcionCorta + " | " + descripcionDetallada;

            String idNotificacion = "TRASP" + idSalidaRemitente + (esRechazado ? "R" : "A");

            String sql = """
                INSERT INTO Notificaciones (id, fecha, descripcion, estado)
                VALUES (?, ?, ?, ?)
            """;

            try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
                ps.setString(1, idNotificacion);
                ps.setDate(2, Date.valueOf(LocalDate.now()));
                ps.setString(3, descripcionFinal);
                ps.setString(4, "activo");
                ps.executeUpdate();
            }
        }
    }

    private static String obtenerDescripcionTraspaso(Connection connOrigen, int idSalidaRemitente) throws SQLException {
        String sql = """
            SELECT 
                d.claveProductoSalida,
                d.cantidad,
                p.nombre AS producto
            FROM detalle_Salida d
            LEFT JOIN productos p ON d.claveProductoSalida = p.id
            WHERE d.claveSalida = ?
            ORDER BY d.idDetalleSalida
        """;

        StringBuilder descripcion = new StringBuilder();

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idSalidaRemitente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String claveProducto = rs.getString("claveProductoSalida");
                    String producto = rs.getString("producto");
                    int cantidad = rs.getInt("cantidad");

                    if (descripcion.length() > 0) {
                        descripcion.append(", ");
                    }

                    descripcion.append(cantidad)
                            .append(" de ")
                            .append(producto != null && !producto.isBlank() ? producto : claveProducto);
                }
            }
        }

        return descripcion.length() > 0 ? descripcion.toString() : "Sin detalle de productos";
    }

    private static String obtenerFechaSalida(Connection connOrigen, int idSalidaRemitente) throws SQLException {
        String sql = """
            SELECT fechaSalida
            FROM salidas
            WHERE idSalida = ?
            LIMIT 1
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idSalidaRemitente);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date fecha = rs.getDate("fechaSalida");
                    return fecha != null ? fecha.toString() : "fecha desconocida";
                }
            }
        }

        return "fecha desconocida";
    }
}