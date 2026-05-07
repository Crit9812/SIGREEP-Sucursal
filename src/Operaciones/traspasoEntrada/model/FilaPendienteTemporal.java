package Operaciones.traspasoEntrada.model;

public class FilaPendienteTemporal {
    String clave;
    String fecha;
    String hora;
    String total;
    String idRemitente;
    String nota;

    FilaPendienteTemporal(String clave,
                          String fecha,
                          String hora,
                          String total,
                          String idRemitente,
                          String nota) {
        this.clave = clave;
        this.fecha = fecha;
        this.hora = hora;
        this.total = total;
        this.idRemitente = idRemitente;
        this.nota = nota;
    }
}