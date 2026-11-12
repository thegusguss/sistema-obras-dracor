package pe.dracor.sgsao.service;

import java.util.List;
import java.util.Map;

/** Datos del panel de obras (RF-18). */
public class PanelResumen {

    private final Map<String, Long> obrasPorEstado;
    private final Map<String, Long> propuestasPorEstado;
    private final int totalObras;
    private final long retrasadas;
    private final int vencidas;
    private final int porVencer;
    private final long incidenciasAbiertas;
    private final double avancePromedio;
    private final List<ObraResumen> obrasActivas;

    public PanelResumen(Map<String, Long> obrasPorEstado, Map<String, Long> propuestasPorEstado, int totalObras,
                        long retrasadas, int vencidas, int porVencer, long incidenciasAbiertas,
                        double avancePromedio, List<ObraResumen> obrasActivas) {
        this.obrasPorEstado = obrasPorEstado;
        this.propuestasPorEstado = propuestasPorEstado;
        this.totalObras = totalObras;
        this.retrasadas = retrasadas;
        this.vencidas = vencidas;
        this.porVencer = porVencer;
        this.incidenciasAbiertas = incidenciasAbiertas;
        this.avancePromedio = avancePromedio;
        this.obrasActivas = obrasActivas;
    }

    public Map<String, Long> getObrasPorEstado() {
        return obrasPorEstado;
    }

    public Map<String, Long> getPropuestasPorEstado() {
        return propuestasPorEstado;
    }

    public int getTotalObras() {
        return totalObras;
    }

    public long getRetrasadas() {
        return retrasadas;
    }

    public int getVencidas() {
        return vencidas;
    }

    public int getPorVencer() {
        return porVencer;
    }

    public long getIncidenciasAbiertas() {
        return incidenciasAbiertas;
    }

    public double getAvancePromedio() {
        return avancePromedio;
    }

    public List<ObraResumen> getObrasActivas() {
        return obrasActivas;
    }
}
