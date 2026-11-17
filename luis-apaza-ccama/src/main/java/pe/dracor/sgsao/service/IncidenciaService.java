package pe.dracor.sgsao.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.domain.EstadoIncidencia;
import pe.dracor.sgsao.domain.EstadoObra;
import pe.dracor.sgsao.domain.Incidencia;
import pe.dracor.sgsao.domain.Obra;
import pe.dracor.sgsao.repository.IncidenciaRepository;
import pe.dracor.sgsao.repository.ObraRepository;
import pe.dracor.sgsao.repository.UsuarioRepository;

/** RF-13: incidencias de una obra (RN-18). */
@Service
public class IncidenciaService {

    private final IncidenciaRepository repo;
    private final ObraRepository obras;
    private final UsuarioRepository usuarios;
    private final HistorialService historial;
    private final UsuarioActual actual;

    public IncidenciaService(IncidenciaRepository repo, ObraRepository obras, UsuarioRepository usuarios,
                             HistorialService historial, UsuarioActual actual) {
        this.repo = repo;
        this.obras = obras;
        this.usuarios = usuarios;
        this.historial = historial;
        this.actual = actual;
    }

    @Transactional(readOnly = true)
    public List<Incidencia> deObra(Long obraId) {
        return repo.findByObraIdOrderByIdDesc(obraId);
    }

    @Transactional(readOnly = true)
    public Incidencia obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new ReglaNegocioException("La incidencia no existe."));
    }

    @Transactional
    public Incidencia registrar(Long obraId, Incidencia form) {
        Obra obra = obras.findById(obraId).orElseThrow(() -> new ReglaNegocioException("La obra no existe."));
        if (obra.getEstado() == EstadoObra.CERRADA) {
            throw new ReglaNegocioException("La obra está cerrada: no se pueden registrar incidencias.");
        }
        if (form.getTipo() == null) {
            throw new ReglaNegocioException("Seleccione el tipo de incidencia.");
        }
        if (form.getDescripcion() == null || form.getDescripcion().isBlank()) {
            throw new ReglaNegocioException("Describa la incidencia.");
        }
        if (form.getFecha() != null && form.getFecha().isAfter(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de la incidencia no puede ser futura.");
        }
        Incidencia i = new Incidencia();
        i.setObra(obra);
        i.setTipo(form.getTipo());
        i.setDescripcion(form.getDescripcion().trim());
        i.setFecha(form.getFecha() == null ? LocalDate.now() : form.getFecha());
        i.setResponsable(form.getResponsable() != null && form.getResponsable().getId() != null
                ? usuarios.findById(form.getResponsable().getId()).orElse(null) : null);
        i.setEstado(EstadoIncidencia.ABIERTA);
        i.setRegistradoPor(actual.get());
        i = repo.save(i);
        historial.registrar("Incidencia", i.getId(), obra.getId(), "CREACION",
                "Se registró una incidencia de tipo " + i.getTipo().getEtiqueta() + ": " + i.getDescripcion());
        return i;
    }

    @Transactional
    public void resolver(Long id, String solucion, LocalDate fecha) {
        Incidencia i = obtener(id);
        if (i.getEstado() == EstadoIncidencia.RESUELTA) {
            throw new ReglaNegocioException("La incidencia ya está resuelta.");
        }
        if (solucion == null || solucion.isBlank()) {
            throw new ReglaNegocioException("Describa cómo se resolvió la incidencia.");
        }
        LocalDate f = fecha == null ? LocalDate.now() : fecha;
        if (f.isBefore(i.getFecha())) {
            throw new ReglaNegocioException("La fecha de resolución no puede ser anterior a la de la incidencia.");
        }
        i.setEstado(EstadoIncidencia.RESUELTA);
        i.setSolucion(solucion.trim());
        i.setFechaResolucion(f);
        repo.save(i);
        historial.registrar("Incidencia", i.getId(), i.getObra().getId(), "MODIFICACION",
                "Se marcó como resuelta la incidencia #" + i.getId() + ": " + i.getSolucion());
    }
}
