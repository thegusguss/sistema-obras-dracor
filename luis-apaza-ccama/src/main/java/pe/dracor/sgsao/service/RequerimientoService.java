package pe.dracor.sgsao.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.domain.Requerimiento;
import pe.dracor.sgsao.repository.ClienteRepository;
import pe.dracor.sgsao.repository.RequerimientoRepository;

/** RF-04: registro de requerimientos de clientes y entidades. */
@Service
public class RequerimientoService {

    private final RequerimientoRepository repo;
    private final ClienteRepository clientes;
    private final HistorialService historial;
    private final UsuarioActual actual;

    public RequerimientoService(RequerimientoRepository repo, ClienteRepository clientes,
                                HistorialService historial, UsuarioActual actual) {
        this.repo = repo;
        this.clientes = clientes;
        this.historial = historial;
        this.actual = actual;
    }

    @Transactional(readOnly = true)
    public List<Requerimiento> listar() {
        return repo.findAllByOrderByFechaRecepcionDescIdDesc();
    }

    @Transactional(readOnly = true)
    public Requerimiento obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new ReglaNegocioException("El requerimiento no existe."));
    }

    @Transactional
    public Requerimiento guardar(Requerimiento form) {
        boolean nuevo = form.getId() == null;
        if (form.getCliente() == null || form.getCliente().getId() == null) {
            throw new ReglaNegocioException("Seleccione el cliente o la entidad.");
        }
        if (form.getDescripcion() == null || form.getDescripcion().isBlank()) {
            throw new ReglaNegocioException("Ingrese la descripción del requerimiento.");
        }
        if (form.getOrigen() == null) {
            throw new ReglaNegocioException("Seleccione el origen del requerimiento.");
        }
        if (form.getFechaRecepcion() == null) {
            throw new ReglaNegocioException("Ingrese la fecha de recepción.");
        }
        if (form.getFechaLimite() != null && form.getFechaLimite().isBefore(form.getFechaRecepcion())) {
            throw new ReglaNegocioException("La fecha límite no puede ser anterior a la fecha de recepción.");
        }
        Requerimiento r = nuevo ? new Requerimiento() : obtener(form.getId());
        r.setCliente(clientes.findById(form.getCliente().getId())
                .orElseThrow(() -> new ReglaNegocioException("El cliente seleccionado no existe.")));
        r.setDescripcion(form.getDescripcion().trim());
        r.setOrigen(form.getOrigen());
        r.setFechaRecepcion(form.getFechaRecepcion());
        r.setFechaLimite(form.getFechaLimite());
        if (nuevo) {
            r.setRegistradoPor(actual.get());
        }
        r = repo.save(r);
        historial.registrar("Requerimiento", r.getId(), null, nuevo ? "CREACION" : "MODIFICACION",
                (nuevo ? "Se registró el requerimiento " : "Se modificó el requerimiento ") + String.format("REQ-%04d", r.getId())
                        + " del cliente " + r.getCliente().getRazonSocial());
        return r;
    }
}
