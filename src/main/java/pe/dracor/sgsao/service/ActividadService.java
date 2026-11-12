package pe.dracor.sgsao.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.domain.Actividad;
import pe.dracor.sgsao.domain.EstadoObra;
import pe.dracor.sgsao.domain.Obra;
import pe.dracor.sgsao.domain.Usuario;
import pe.dracor.sgsao.repository.ActividadRepository;
import pe.dracor.sgsao.repository.ObraRepository;
import pe.dracor.sgsao.repository.UsuarioRepository;

/** RF-10: planificacion de actividades y asignacion de responsables (RN-09). */
@Service
public class ActividadService {

    private final ActividadRepository repo;
    private final ObraRepository obras;
    private final UsuarioRepository usuarios;
    private final ObraService obraService;
    private final HistorialService historial;

    public ActividadService(ActividadRepository repo, ObraRepository obras, UsuarioRepository usuarios,
                            ObraService obraService, HistorialService historial) {
        this.repo = repo;
        this.obras = obras;
        this.usuarios = usuarios;
        this.obraService = obraService;
        this.historial = historial;
    }

    @Transactional(readOnly = true)
    public List<Actividad> deObra(Long obraId) {
        return repo.findByObraIdOrderByFechaInicioAscIdAsc(obraId);
    }

    @Transactional(readOnly = true)
    public Actividad obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new ReglaNegocioException("La actividad no existe."));
    }

    @Transactional
    public Actividad guardar(Actividad form, Long obraId) {
        boolean nuevo = form.getId() == null;
        Actividad a = nuevo ? new Actividad() : obtener(form.getId());
        Obra obra = nuevo ? obras.findById(obraId).orElseThrow(() -> new ReglaNegocioException("La obra no existe."))
                : a.getObra();
        if (obra.getEstado() == EstadoObra.CERRADA) {
            throw new ReglaNegocioException("La obra está cerrada: no se pueden modificar sus actividades.");
        }
        if (form.getNombre() == null || form.getNombre().isBlank()) {
            throw new ReglaNegocioException("Ingrese el nombre de la actividad.");
        }
        if (form.getFechaInicio() == null || form.getFechaFin() == null) {
            throw new ReglaNegocioException("Ingrese las fechas previstas de la actividad.");
        }
        if (form.getFechaFin().isBefore(form.getFechaInicio())) {
            throw new ReglaNegocioException("La fecha de fin de la actividad no puede ser anterior a la de inicio.");
        }
        if (form.getFechaInicio().isBefore(obra.getFechaInicio()) || form.getFechaFin().isAfter(obra.getFechaFin())) {
            throw new ReglaNegocioException("Las fechas de la actividad deben estar dentro del plazo de la obra ("
                    + obra.getFechaInicio() + " a " + obra.getFechaFin() + ").");
        }
        if (form.getPeso() < 1 || form.getPeso() > 100) {
            throw new ReglaNegocioException("El peso de la actividad debe estar entre 1 y 100.");
        }
        if (form.getResponsable() == null || form.getResponsable().getId() == null) {
            throw new ReglaNegocioException("Seleccione el responsable de la actividad.");
        }
        Usuario resp = usuarios.findById(form.getResponsable().getId())
                .orElseThrow(() -> new ReglaNegocioException("El responsable no existe."));
        if (!resp.isActivo()) {
            throw new ReglaNegocioException("El responsable seleccionado está desactivado.");
        }
        String respAnterior = (!nuevo && a.getResponsable() != null) ? a.getResponsable().getNombreCompleto() : null;
        a.setObra(obra);
        a.setNombre(form.getNombre().trim());
        a.setDescripcion(form.getDescripcion() == null || form.getDescripcion().isBlank() ? null : form.getDescripcion().trim());
        a.setResponsable(resp);
        a.setFechaInicio(form.getFechaInicio());
        a.setFechaFin(form.getFechaFin());
        a.setPeso(form.getPeso());
        if (nuevo) {
            a.setAvance(0);
            a.setUltimaActualizacion(LocalDateTime.now());
        }
        a = repo.save(a);
        String detalle = (nuevo ? "Se planificó la actividad «" : "Se modificó la actividad «") + a.getNombre()
                + "» (responsable: " + resp.getNombreCompleto() + ", peso " + a.getPeso() + ").";
        if (respAnterior != null && !respAnterior.equals(resp.getNombreCompleto())) {
            detalle += " Se reasignó el responsable: antes " + respAnterior + ".";
        }
        historial.registrar("Actividad", a.getId(), obra.getId(), nuevo ? "CREACION" : "MODIFICACION", detalle);
        obraService.recalcularEstado(obra.getId());
        return a;
    }

    @Transactional
    public void reasignar(Long actividadId, Long responsableId) {
        Actividad a = obtener(actividadId);
        if (a.getObra().getEstado() == EstadoObra.CERRADA) {
            throw new ReglaNegocioException("La obra está cerrada: no se pueden modificar sus actividades.");
        }
        Usuario nuevo = usuarios.findById(responsableId)
                .orElseThrow(() -> new ReglaNegocioException("El responsable no existe."));
        if (!nuevo.isActivo()) {
            throw new ReglaNegocioException("El responsable seleccionado está desactivado.");
        }
        String antes = a.getResponsable() == null ? "sin responsable" : a.getResponsable().getNombreCompleto();
        a.setResponsable(nuevo);
        repo.save(a);
        historial.registrar("Actividad", a.getId(), a.getObra().getId(), "MODIFICACION",
                "Se reasignó la actividad «" + a.getNombre() + "»: de " + antes + " a " + nuevo.getNombreCompleto() + ".");
    }

    /** Permite cerrar una obra con una actividad que no se ejecutara (RN-16). */
    @Transactional
    public void justificar(Long actividadId, String texto) {
        Actividad a = obtener(actividadId);
        if (a.getObra().getEstado() == EstadoObra.CERRADA) {
            throw new ReglaNegocioException("La obra está cerrada: no se pueden modificar sus actividades.");
        }
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("Escriba la justificación de la actividad.");
        }
        a.setJustificada(true);
        a.setJustificacion(texto.trim());
        repo.save(a);
        historial.registrar("Actividad", a.getId(), a.getObra().getId(), "MODIFICACION",
                "Se justificó la actividad «" + a.getNombre() + "»: " + a.getJustificacion());
    }
}
