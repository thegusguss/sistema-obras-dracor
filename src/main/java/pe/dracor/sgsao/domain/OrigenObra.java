package pe.dracor.sgsao.domain;

public enum OrigenObra {
    PROPUESTA_ACEPTADA("Propuesta aceptada"),
    ORDEN_SERVICIO("Orden de servicio"),
    CONTRATO("Contrato");

    private final String etiqueta;

    OrigenObra(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
