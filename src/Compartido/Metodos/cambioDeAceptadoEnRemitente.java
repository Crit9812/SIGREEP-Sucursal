package Compartido.Metodos;

import conexion.Conexion;
import conexion.ConexionExtra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class cambioDeAceptadoEnRemitente {

    public static boolean ejecutar(Connection connLocal, List<String> clavesEntrada) throws SQLException {
        System.out.println("Este es el apartado para procesar aceptación de traspaso en sucursal origen");

        for (String claveEntrada : filtrarClaves(clavesEntrada)) {
            DatosOrigenTraspaso datos = obtenerDatosOrigenTraspaso(connLocal, claveEntrada);

            if (datos == null
                    || datos.nombreSucursal == null
                    || datos.nombreSucursal.isBlank()
                    || "No especificado".equalsIgnoreCase(datos.nombreSucursal)
                    || datos.idSalidaRemitente == null) {

                System.err.println("No se pudieron obtener datos de origen para entrada: " + claveEntrada);
                return false;
            }

            try (Connection connOrigen = new ConexionExtra().conectar(datos.nombreSucursal)) {
                System.out.println("Conectando a sucursal origen: " + datos.nombreSucursal);
                System.out.println("Salida origen: " + datos.idSalidaRemitente);

                connOrigen.setAutoCommit(false);

                try {
                    actualizarSalidaOrigenATraspasado(connOrigen, datos.idSalidaRemitente);
                    procesarDetallesSalidaOrigen(connOrigen, datos.idSalidaRemitente);
                    actualizarEntradas.ejecutar(connOrigen, datos.idSalidaRemitente);

                    enviarNotificacion.ejecutar(
                            datos.nombreSucursal,
                            Conexion.getNombreBD(),
                            datos.idSalidaRemitente,
                            "aceptado"
                    );

                    connOrigen.commit();
                } catch (Exception e) {
                    connOrigen.rollback();
                    throw e;
                }
            }
        }

        return true;
    }

    private static class DatosOrigenTraspaso {
        String nombreSucursal;
        Integer idSalidaRemitente;

        DatosOrigenTraspaso(String nombreSucursal, Integer idSalidaRemitente) {
            this.nombreSucursal = nombreSucursal;
            this.idSalidaRemitente = idSalidaRemitente;
        }
    }

    private static DatosOrigenTraspaso obtenerDatosOrigenTraspaso(Connection connLocal, String claveEntrada) throws SQLException {
        String sqlEntrada = """
            SELECT idRemitente, idSalidaRemitente
            FROM entradas
            WHERE idEntrada = ?
            LIMIT 1
        """;

        String idRemitente = null;
        Integer idSalidaRemitente = null;

        try (PreparedStatement ps = connLocal.prepareStatement(sqlEntrada)) {
            ps.setString(1, claveEntrada);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idRemitente = rs.getString("idRemitente");

                    Object valorSalida = rs.getObject("idSalidaRemitente");
                    idSalidaRemitente = valorSalida != null ? rs.getInt("idSalidaRemitente") : null;
                }
            }
        }

        if (idRemitente == null || idRemitente.isBlank() || idSalidaRemitente == null) {
            System.err.println("Entrada sin idRemitente o idSalidaRemitente. Entrada: " + claveEntrada);
            return null;
        }

        String nombreSucursal = obtenerNombreSucursalDesdeAlmacen(idRemitente);

        if (nombreSucursal == null
                || nombreSucursal.isBlank()
                || "No especificado".equalsIgnoreCase(nombreSucursal)) {

            System.err.println("No se encontró nombre de sucursal en Almacen para idRemitente: " + idRemitente);
            return null;
        }

        return new DatosOrigenTraspaso(nombreSucursal, idSalidaRemitente);
    }

    private static String obtenerNombreSucursalDesdeAlmacen(String idRemitente) throws SQLException {
        String sql = """
            SELECT nombre
            FROM sucursales
            WHERE id = ?
            LIMIT 1
        """;

        try (Connection connAlmacen = new ConexionExtra().conectar("Almacen");
             PreparedStatement ps = connAlmacen.prepareStatement(sql)) {

            ps.setString(1, idRemitente);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre");
                    return nombre != null && !nombre.isBlank() ? nombre : null;
                }
            }
        }

        return null;
    }

    private static void actualizarSalidaOrigenATraspasado(Connection connOrigen, int idSalida) throws SQLException {
        String sql = "UPDATE salidas SET Estado = ? WHERE idSalida = ?";

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setString(1, "traspasado");
            ps.setInt(2, idSalida);
            ps.executeUpdate();
        }
    }

    private static void procesarDetallesSalidaOrigen(Connection connOrigen, int idSalida) throws SQLException {
        String sql = """
            SELECT idDetalleSalida, tipoDetalle
            FROM detalle_Salida
            WHERE claveSalida = ?
            ORDER BY idDetalleSalida
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idSalida);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idDetalleSalida = rs.getInt("idDetalleSalida");
                    int tipoDetalle = rs.getInt("tipoDetalle");

                    actualizarDetalleSalidaATraspasado(connOrigen, idDetalleSalida);

                    if (tipoDetalle == 1) {
                        procesarDetalleSalidaSegmentado(connOrigen, idDetalleSalida);
                    } else {
                        procesarDetalleSalidaNormal(connOrigen, idDetalleSalida);
                    }
                }
            }
        }
    }

    private static void actualizarDetalleSalidaATraspasado(Connection connOrigen, int idDetalleSalida) throws SQLException {
        String sql = "UPDATE detalle_Salida SET estado = ? WHERE idDetalleSalida = ?";

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setString(1, "traspasado");
            ps.setInt(2, idDetalleSalida);
            ps.executeUpdate();
        }
    }

    private static void procesarDetalleSalidaSegmentado(Connection connOrigen, int idDetalleSalida) throws SQLException {
        String sql = """
            SELECT idDetalle, idArticulo
            FROM detalleArticulo
            WHERE idDetalleSalida = ?
            ORDER BY idDetalle
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setInt(1, idDetalleSalida);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String idDetalleArticulo = rs.getString("idDetalle");
                    String idArticulo = rs.getString("idArticulo");

                    actualizarDetalleArticuloATraspasado(connOrigen, idDetalleArticulo);

                    if (idArticulo != null && !idArticulo.isBlank()) {
                        validacionDeEstadoDeSegmentado.ejecutar(connOrigen, idArticulo);
                    }
                }
            }
        }
    }

    private static void actualizarDetalleArticuloATraspasado(Connection connOrigen, String idDetalleArticulo) throws SQLException {
        String sql = "UPDATE detalleArticulo SET estado = ? WHERE idDetalle = ?";

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setString(1, "traspasado");
            ps.setString(2, idDetalleArticulo);
            ps.executeUpdate();
        }
    }

    private static void procesarDetalleSalidaNormal(Connection connOrigen, int idDetalleSalida) throws SQLException {
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
                    actualizarEstadoArticulo.ejecutar(connOrigen, String.valueOf(idArticulo), "traspasado");
                }
            }
        }
    }

    private static List<String> filtrarClaves(List<String> clavesEntrada) {
        List<String> claves = new ArrayList<>();

        if (clavesEntrada == null) {
            return claves;
        }

        for (String clave : clavesEntrada) {
            if (clave != null && !clave.isBlank()) {
                claves.add(clave.trim());
            }
        }

        return claves;
    }
}