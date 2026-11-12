package pe.dracor.sgsao.domain;

public enum EstadoObra {
    PLANIFICADA("Planificada"),
    EN_EJECUCION("En ejecución"),
    PARALIZADA("Paralizada"),
    FINALIZADA("Finalizada"),
    CERRADA("Cerrada");

    private final String etiqueta;

    EstadoObra(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
