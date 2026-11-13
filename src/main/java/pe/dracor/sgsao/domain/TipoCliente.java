package pe.dracor.sgsao.domain;

public enum TipoCliente {
    ENTIDAD_PUBLICA("Entidad pública"),
    CLIENTE_PRIVADO("Cliente privado");

    private final String etiqueta;

    TipoCliente(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
