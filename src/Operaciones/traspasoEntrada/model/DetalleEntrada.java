package Operaciones.traspasoEntrada.model;

/*
 * Clase usada para representar cada detalle de una entrada.
 * Sirve para mostrar productos, cantidades, precios y sucursal en la vista.
 */
public class DetalleEntrada {

    private String claveProducto;
    private String producto;
    private String cantidad;
    private String precioUnitario;
    private String precioTotal;
    private String nombreSucursal;
    private String nota;

    /*
     * Constructor del detalle.
     * nombreSucursal se asegura como vacío si llega null.
     */
    public DetalleEntrada(String claveProducto, String producto, String cantidad,
                          String precioUnitario, String precioTotal, String nombreSucursal) {
        this.claveProducto = claveProducto;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.precioTotal = precioTotal;
        this.nombreSucursal = nombreSucursal != null ? nombreSucursal : "";
    }

    public String getClaveProducto() {
        return claveProducto;
    }

    public String getProducto() {
        return producto;
    }

    public String getCantidad() {
        return cantidad;
    }

    public String getPrecioUnitario() {
        return precioUnitario;
    }

    public String getPrecioTotal() {
        return precioTotal;
    }

    public String getNombreSucursal() {
        return nombreSucursal;
    }

    public String getNota() { return nota; }

    public void setNota(String nota) { this.nota = nota; }

}