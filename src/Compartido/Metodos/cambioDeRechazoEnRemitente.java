package Compartido.Metodos;

import conexion.Conexion;
import conexion.ConexionExtra;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class cambioDeRechazoEnRemitente {

    public static boolean ejecutar(Connection connLocal, List<String> clavesEntrada) throws SQLException {
        System.out.println("Este es el apartado para procesar rechazo de traspaso en sucursal origen");

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
                connOrigen.setAutoCommit(false);

                try {

                    enviarNotificacion.ejecutar(
                            datos.nombreSucursal,
                            Conexion.getNombreBD(),
                            datos.idSalidaRemitente,
                            "rechazado"
                    );

                    actualizarSalidaOrigenARechazado(connOrigen, datos.idSalidaRemitente);
                    procesarDetallesSalidaRechazo(connOrigen, datos.idSalidaRemitente);

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
            return null;
        }

        String nombreSucursal = obtenerNombreSucursalDesdeAlmacen(idRemitente);

        if (nombreSucursal == null || nombreSucursal.isBlank()) {
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

    private static void actualizarSalidaOrigenARechazado(Connection connOrigen, int idSalida) throws SQLException {
        String sql = """
            UPDATE salidas
            SET Estado = ?
            WHERE idSalida = ?
        """;

        try (PreparedStatement ps = connOrigen.prepareStatement(sql)) {
            ps.setString(1, "rechazado");
            ps.setInt(2, idSalida);
            ps.executeUpdate();
        }
    }

    private static void procesarDetallesSalidaRechazo(Connection connOrigen, int idSalida) throws SQLException {
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

                    eliminarDetalleSalida.ejecutar(connOrigen, idSalida, idDetalleSalida, tipoDetalle);
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