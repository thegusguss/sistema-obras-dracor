package pe.dracor.sgsao.domain;

public enum TipoRespuesta {
    RESPUESTA("Respuesta"),
    OBSERVACION("Observación");

    private final String etiqueta;

    TipoRespuesta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
