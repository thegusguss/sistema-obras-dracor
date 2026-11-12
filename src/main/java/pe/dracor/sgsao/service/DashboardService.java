package pe.dracor.sgsao.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.domain.EstadoIncidencia;
import pe.dracor.sgsao.domain.EstadoObra;
import pe.dracor.sgsao.domain.EstadoPropuesta;
import pe.dracor.sgsao.repository.IncidenciaRepository;
import pe.dracor.sgsao.repository.PropuestaRepository;

/** RF-18: resumen de obras por estado, avance y alertas. */
@Service
public class DashboardService {

    private final ObraService obras;
    private final AlertaService alertas;
    private final PropuestaRepository propuestas;
    private final IncidenciaRepository incidencias;

    public DashboardService(ObraService obras, AlertaService alertas, PropuestaRepository propuestas,
                            IncidenciaRepository incidencias) {
        this.obras = obras;
        this.alertas = alertas;
        this.propuestas = propuestas;
        this.incidencias = incidencias;
    }

    @Transactional(readOnly = true)
    public PanelResumen resumen() {
        List<ObraResumen> todas = obras.resumenes();
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoObra e : EstadoObra.values()) {
            porEstado.put(e.getEtiqueta(), 0L);
        }
        porEstado.put("Retrasada", 0L);
        long retrasadas = 0;
        double suma = 0;
        int activas = 0;
        List<ObraResumen> obrasActivas = new ArrayList<>();
        for (ObraResumen r : todas) {
            String clave = r.isRetrasada() ? "Retrasada" : r.getObra().getEstado().getEtiqueta();
            porEstado.put(clave, porEstado.get(clave) + 1);
            if (r.isRetrasada()) {
                retrasadas++;
            }
            EstadoObra e = r.getObra().getEstado();
            if (e == EstadoObra.PLANIFICADA || e == EstadoObra.EN_EJECUCION || e == EstadoObra.PARALIZADA) {
                suma += r.getAvance();
                activas++;
                if (obrasActivas.size() < 8) {
                    obrasActivas.add(r);
                }
            }
        }
        Map<String, Long> props = new LinkedHashMap<>();
        for (EstadoPropuesta e : EstadoPropuesta.values()) {
            props.put(e.getEtiqueta(), propuestas.countByEstado(e));
        }
        double promedio = activas == 0 ? 0 : Math.round(suma / activas * 10.0) / 10.0;
        return new PanelResumen(porEstado, props, todas.size(), retrasadas, alertas.vencidas().size(),
                alertas.porVencer().size(), incidencias.countByEstado(EstadoIncidencia.ABIERTA), promedio, obrasActivas);
    }
}
