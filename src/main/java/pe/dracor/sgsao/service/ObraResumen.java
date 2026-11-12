package pe.dracor.sgsao.service;

import pe.dracor.sgsao.domain.Obra;

/** Obra con sus indicadores calculados (avance, actividades vencidas e incidencias abiertas). */
public class ObraResumen {

    private final Obra obra;
    private final double avance;
    private final int totalActividades;
    private final int terminadas;
    private final int vencidas;
    private final long incidenciasAbiertas;
    private final boolean retrasada;

    public ObraResumen(Obra obra, double avance, int totalActividades, int terminadas, int vencidas,
                       long incidenciasAbiertas, boolean retrasada) {
        this.obra = obra;
        this.avance = avance;
        this.totalActividades = totalActividades;
        this.terminadas = terminadas;
        this.vencidas = vencidas;
        this.incidenciasAbiertas = incidenciasAbiertas;
        this.retrasada = retrasada;
    }

    public Obra getObra() {
        return obra;
    }

    public double getAvance() {
        return avance;
    }

    public int getAvanceEntero() {
        return (int) Math.round(avance);
    }

    public int getTotalActividades() {
        return totalActividades;
    }

    public int getTerminadas() {
        return terminadas;
    }

    public int getVencidas() {
        return vencidas;
    }

    public long getIncidenciasAbiertas() {
        return incidenciasAbiertas;
    }

    public boolean isRetrasada() {
        return retrasada;
    }

    /** RN-15: una obra con actividades vencidas se muestra como retrasada. */
    public String getEstadoTexto() {
        return retrasada ? "Retrasada" : obra.getEstado().getEtiqueta();
    }

    public String getBadge() {
        if (retrasada) {
            return "danger";
        }
        switch (obra.getEstado()) {
            case EN_EJECUCION:
                return "primary";
            case PARALIZADA:
                return "warning";
            case FINALIZADA:
                return "success";
            case CERRADA:
                return "dark";
            default:
                return "secondary";
        }
    }
}
