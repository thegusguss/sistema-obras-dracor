package pe.dracor.sgsao.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.domain.HistorialActualizacion;
import pe.dracor.sgsao.repository.HistorialActualizacionRepository;

/** RF-15 y RN-19: registra y consulta el historial de actualizaciones (usuario, fecha y hora). */
@Service
public class HistorialService {

    private final HistorialActualizacionRepository repo;
    private final UsuarioActual usuarioActual;

    public HistorialService(HistorialActualizacionRepository repo, UsuarioActual usuarioActual) {
        this.repo = repo;
        this.usuarioActual = usuarioActual;
    }

    @Transactional
    public void registrar(String entidad, Long entidadId, Long obraId, String accion, String detalle) {
        HistorialActualizacion h = new HistorialActualizacion();
        h.setEntidad(entidad);
        h.setEntidadId(entidadId);
        h.setObraId(obraId);
        h.setAccion(accion);
        h.setDetalle(detalle != null && detalle.length() > 790 ? detalle.substring(0, 790) : detalle);
        h.setUsuario(usuarioActual.nombre());
        h.setFechaHora(LocalDateTime.now());
        repo.save(h);
    }

    @Transactional(readOnly = true)
    public List<HistorialActualizacion> deObra(Long obraId) {
        return repo.findByObraIdOrderByFechaHoraDescIdDesc(obraId);
    }

    @Transactional(readOnly = true)
    public List<HistorialActualizacion> ultimos(String entidad) {
        if (entidad == null || entidad.isBlank()) {
            return repo.findTop200ByOrderByFechaHoraDescIdDesc();
        }
        return repo.findTop200ByEntidadOrderByFechaHoraDescIdDesc(entidad);
    }
}
