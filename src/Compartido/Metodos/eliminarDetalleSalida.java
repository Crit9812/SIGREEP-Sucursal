package Compartido.Metodos;

import java.sql.Connection;
import java.sql.SQLException;

public class eliminarDetalleSalida {

    public static void ejecutar(Connection connOrigen,
                                int idSalida,
                                int idDetalleSalida,
                                int tipoDetalle) throws SQLException {

        actualizarDetalleSalidaARechazado.ejecutar(connOrigen, idDetalleSalida);

        if (tipoDetalle == 0) {
            eliminarArticulosSalidaNormal.ejecutar(connOrigen, idSalida, idDetalleSalida);
        } else if (tipoDetalle == 1) {
            eliminarArticulosSalidaSegmentado.ejecutar(connOrigen, idSalida, idDetalleSalida);
        }
    }
}