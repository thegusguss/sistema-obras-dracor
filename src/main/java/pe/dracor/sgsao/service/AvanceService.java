package pe.dracor.sgsao.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.domain.Actividad;
import pe.dracor.sgsao.domain.AvanceActividad;
import pe.dracor.sgsao.domain.EstadoObra;
import pe.dracor.sgsao.repository.ActividadRepository;
import pe.dracor.sgsao.repository.AvanceActividadRepository;

/** RF-11: registro del porcentaje de avance de una actividad (RN-10, RN-14). */
@Service
public class AvanceService {

    private final AvanceActividadRepository repo;
    private final ActividadRepository actividades;
    private final ObraService obraService;
    private final HistorialService historial;
    private final UsuarioActual actual;

    public AvanceService(AvanceActividadRepository repo, ActividadRepository actividades,
                         ObraService obraService, HistorialService historial, UsuarioActual actual) {
        this.repo = repo;
        this.actividades = actividades;
        this.obraService = obraService;
        this.historial = historial;
        this.actual = actual;
    }

    @Transactional(readOnly = true)
    public List<AvanceActividad> deActividad(Long actividadId) {
        return repo.findByActividadIdOrderByFechaRegistroDescIdDesc(actividadId);
    }

    @Transactional
    public AvanceActividad registrar(Long actividadId, Integer porcentaje, LocalDate fecha,
                                     String observacion, String motivoRetroceso) {
        Actividad a = actividades.findById(actividadId)
                .orElseThrow(() -> new ReglaNegocioException("La actividad no existe."));
        EstadoObra estadoObra = a.getObra().getEstado();
        if (estadoObra == EstadoObra.CERRADA) {
            throw new ReglaNegocioException("La obra está cerrada: no se pueden registrar avances.");
        }
        if (estadoObra == EstadoObra.PARALIZADA) {
            throw new ReglaNegocioException("La obra está paralizada. Reanúdela para registrar avances.");
        }
        if (porcentaje == null || porcentaje < 0 || porcentaje > 100) {
            throw new ReglaNegocioException("El porcentaje de avance debe ser un número entero entre 0 y 100.");
        }
        LocalDate f = fecha == null ? LocalDate.now() : fecha;
        if (f.isAfter(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha del avance no puede ser futura.");
        }
        if (porcentaje < a.getAvance() && (motivoRetroceso == null || motivoRetroceso.isBlank())) {
            throw new ReglaNegocioException("El avance disminuye de " + a.getAvance() + " % a " + porcentaje
                    + " %: indique el motivo.");
        }
        if (porcentaje == a.getAvance() && (observacion == null || observacion.isBlank())) {
            throw new ReglaNegocioException("El porcentaje no cambia. Ingrese una observación o un nuevo porcentaje.");
        }
        int anterior = a.getAvance();
        AvanceActividad av = new AvanceActividad();
        av.setActividad(a);
        av.setPorcentaje(porcentaje);
        av.setFecha(f);
        av.setObservacion(observacion == null || observacion.isBlank() ? null : observacion.trim());
        av.setMotivoRetroceso(porcentaje < anterior ? motivoRetroceso.trim() : null);
        av.setUsuario(actual.get());
        av = repo.save(av);

        a.setAvance(porcentaje);
        a.setUltimaActualizacion(LocalDateTime.now());
        actividades.save(a);

        historial.registrar("Avance", av.getId(), a.getObra().getId(), "AVANCE",
                "Actividad «" + a.getNombre() + "»: avance de " + anterior + " % a " + porcentaje + " %."
                        + (av.getObservacion() == null ? "" : " " + av.getObservacion()));
        obraService.recalcularEstado(a.getObra().getId());
        return av;
    }
}
