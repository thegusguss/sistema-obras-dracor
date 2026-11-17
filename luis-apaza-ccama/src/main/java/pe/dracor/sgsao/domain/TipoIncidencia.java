package pe.dracor.sgsao.domain;

public enum TipoIncidencia {
    CLIMATICA("Climática"),
    MATERIALES("Materiales"),
    PERSONAL("Personal"),
    EQUIPOS("Equipos y maquinaria"),
    SEGURIDAD("Seguridad"),
    TECNICA("Técnica"),
    ADMINISTRATIVA("Administrativa"),
    OTRA("Otra");

    private final String etiqueta;

    TipoIncidencia(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
