package pe.dracor.sgsao.util;

/** Evita redirecciones a sitios externos: solo se aceptan rutas internas. */
public final class Redireccion {

    private Redireccion() {
    }

    public static String seguro(String volver, String porDefecto) {
        if (volver != null && volver.startsWith("/") && !volver.startsWith("//") && !volver.contains("://")) {
            return "redirect:" + volver;
        }
        return "redirect:" + porDefecto;
    }
}
