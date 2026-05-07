package Compartido.Metodos;

import conexion.Conexion;
import conexion.ConexionExtra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class cargarDatosSucursalActual {

    public static DatosSucursal ejecutar() {
        String nombreSucursal = Conexion.getNombreBD();

        if (nombreSucursal == null || nombreSucursal.isBlank()) {
            return null;
        }

        String sql = """
                SELECT nombre, domicilio, cp, colonia, numeroInt, numeroExt,
                       ciudad, estado, localidad, pais, correo, telefono
                FROM sucursales
                WHERE LOWER(nombre) = LOWER(?)
                LIMIT 1
                """;

        try (Connection conn = new ConexionExtra().conectar("Almacen");
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreSucursal);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DatosSucursal datos = new DatosSucursal();

                    datos.nombre = valorTexto(rs.getString("nombre"));
                    datos.domicilio = valorTexto(rs.getString("domicilio"));
                    datos.cp = valorTexto(rs.getObject("cp"));
                    datos.colonia = valorTexto(rs.getString("colonia"));
                    datos.numeroInt = valorTexto(rs.getObject("numeroInt"));
                    datos.numeroExt = valorTexto(rs.getObject("numeroExt"));
                    datos.ciudad = valorTexto(rs.getString("ciudad"));
                    datos.estado = valorTexto(rs.getString("estado"));
                    datos.localidad = valorTexto(rs.getString("localidad"));
                    datos.pais = valorTexto(rs.getString("pais"));
                    datos.correo = valorTexto(rs.getString("correo"));
                    datos.telefono = valorTexto(rs.getString("telefono"));

                    return datos;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String valorTexto(Object value) {
        return value == null ? "--" : value.toString();
    }

    // Clase para transportar datos
    public static class DatosSucursal {
        public String nombre;
        public String domicilio;
        public String cp;
        public String colonia;
        public String numeroInt;
        public String numeroExt;
        public String ciudad;
        public String estado;
        public String localidad;
        public String pais;
        public String correo;
        public String telefono;
    }
}