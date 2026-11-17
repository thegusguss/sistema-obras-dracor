package pe.dracor.sgsao.domain;

public enum EstadoIncidencia {
    ABIERTA("Abierta"),
    RESUELTA("Resuelta");

    private final String etiqueta;

    EstadoIncidencia(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
