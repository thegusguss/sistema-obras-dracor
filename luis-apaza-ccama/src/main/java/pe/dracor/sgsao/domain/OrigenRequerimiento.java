package pe.dracor.sgsao.domain;

public enum OrigenRequerimiento {
    CONTACTO_DIRECTO("Contacto directo"),
    CONVOCATORIA("Convocatoria"),
    CORREO("Correo electrónico"),
    WHATSAPP("WhatsApp"),
    DOCUMENTO_FISICO("Documento físico"),
    OTRO("Otro");

    private final String etiqueta;

    OrigenRequerimiento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
