package pe.dracor.sgsao.domain;

public enum Rol {
    ADMINISTRADOR("Administrador"),
    JEFE_PROYECTO("Jefe de proyecto"),
    SUPERVISOR("Supervisor"),
    ADMINISTRATIVO("Administrativo");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
