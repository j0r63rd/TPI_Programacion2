package entities;

import java.time.LocalDate;

public class CodigoBarras {
    private Long id;
    private Boolean eliminado;
    private TipoCodigo tipo;
    private String valor;
    private LocalDate fechaAsignacion;
    private String observaciones;
    private Long productoId; // FK hacia Producto

    public CodigoBarras() {}

    public CodigoBarras(Long id, Boolean eliminado, TipoCodigo tipo, String valor,
                         LocalDate fechaAsignacion, String observaciones, Long productoId) {
        this.id = id;
        this.eliminado = eliminado;
        this.tipo = tipo;
        this.valor = valor;
        this.fechaAsignacion = fechaAsignacion;
        this.observaciones = observaciones;
        this.productoId = productoId;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Boolean getEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }

    public TipoCodigo getTipo() { return tipo; }
    public void setTipo(TipoCodigo tipo) { this.tipo = tipo; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public LocalDate getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDate fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    @Override
    public String toString() {
        return "CodigoBarras{" +
                "id=" + id +
                ", eliminado=" + eliminado +
                ", tipo=" + tipo +
                ", valor='" + valor + '\'' +
                ", fechaAsignacion=" + fechaAsignacion +
                ", observaciones='" + observaciones + '\'' +
                ", productoId=" + productoId +
                '}';
    }
}