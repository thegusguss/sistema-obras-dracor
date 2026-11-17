package pe.dracor.sgsao.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.domain.EstadoPropuesta;
import pe.dracor.sgsao.domain.Propuesta;
import pe.dracor.sgsao.domain.RespuestaPropuesta;
import pe.dracor.sgsao.domain.TipoRespuesta;
import pe.dracor.sgsao.repository.PropuestaRepository;
import pe.dracor.sgsao.repository.RequerimientoRepository;
import pe.dracor.sgsao.repository.RespuestaPropuestaRepository;

/** RF-05 y RF-06: propuestas, cotizaciones, respuestas y observaciones (RN-08). */
@Service
public class PropuestaService {

    private static final Map<EstadoPropuesta, Set<EstadoPropuesta>> TRANSICIONES = new EnumMap<>(EstadoPropuesta.class);

    static {
        TRANSICIONES.put(EstadoPropuesta.EN_PREPARACION, Set.of(EstadoPropuesta.PENDIENTE_RESPUESTA));
        TRANSICIONES.put(EstadoPropuesta.PENDIENTE_RESPUESTA,
                Set.of(EstadoPropuesta.OBSERVADA, EstadoPropuesta.ACEPTADA, EstadoPropuesta.RECHAZADA));
        TRANSICIONES.put(EstadoPropuesta.OBSERVADA,
                Set.of(EstadoPropuesta.PENDIENTE_RESPUESTA, EstadoPropuesta.ACEPTADA, EstadoPropuesta.RECHAZADA));
        TRANSICIONES.put(EstadoPropuesta.ACEPTADA, Set.of());
        TRANSICIONES.put(EstadoPropuesta.RECHAZADA, Set.of());
    }

    private final PropuestaRepository repo;
    private final RequerimientoRepository requerimientos;
    private final RespuestaPropuestaRepository respuestas;
    private final DocumentoService documentos;
    private final HistorialService historial;
    private final UsuarioActual actual;

    public PropuestaService(PropuestaRepository repo, RequerimientoRepository requerimientos,
                            RespuestaPropuestaRepository respuestas, DocumentoService documentos,
                            HistorialService historial, UsuarioActual actual) {
        this.repo = repo;
        this.requerimientos = requerimientos;
        this.respuestas = respuestas;
        this.documentos = documentos;
        this.historial = historial;
        this.actual = actual;
    }

    @Transactional(readOnly = true)
    public List<Propuesta> listar(EstadoPropuesta estado) {
        return estado == null ? repo.findAllByOrderByIdDesc() : repo.findByEstadoOrderByIdDesc(estado);
    }

    @Transactional(readOnly = true)
    public List<Propuesta> porRequerimiento(Long requerimientoId) {
        return repo.findByRequerimientoIdOrderByIdDesc(requerimientoId);
    }

    @Transactional(readOnly = true)
    public List<Propuesta> aceptadas() {
        return repo.findByEstadoOrderByIdDesc(EstadoPropuesta.ACEPTADA);
    }

    @Transactional(readOnly = true)
    public Propuesta obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new ReglaNegocioException("La propuesta no existe."));
    }

    @Transactional(readOnly = true)
    public List<RespuestaPropuesta> respuestasDe(Long propuestaId) {
        return respuestas.findByPropuestaIdOrderByFechaDescIdDesc(propuestaId);
    }

    @Transactional(readOnly = true)
    public Set<EstadoPropuesta> siguientesEstados(Propuesta p) {
        return TRANSICIONES.get(p.getEstado());
    }

    @Transactional
    public Propuesta guardar(Propuesta form) {
        boolean nuevo = form.getId() == null;
        if (form.getRequerimiento() == null || form.getRequerimiento().getId() == null) {
            throw new ReglaNegocioException("Seleccione el requerimiento al que responde la propuesta.");
        }
        if (form.getMonto() == null || form.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("Ingrese un monto mayor que cero.");
        }
        Propuesta p = nuevo ? new Propuesta() : obtener(form.getId());
        if (!nuevo && p.getEstado() != EstadoPropuesta.EN_PREPARACION && p.getEstado() != EstadoPropuesta.OBSERVADA) {
            throw new ReglaNegocioException("Solo se pueden modificar propuestas en preparación u observadas.");
        }
        p.setRequerimiento(requerimientos.findById(form.getRequerimiento().getId())
                .orElseThrow(() -> new ReglaNegocioException("El requerimiento no existe.")));
        p.setMonto(form.getMonto());
        p.setFechaEnvio(form.getFechaEnvio());
        p.setDetalle(form.getDetalle() == null || form.getDetalle().isBlank() ? null : form.getDetalle().trim());
        if (nuevo) {
            p.setEstado(EstadoPropuesta.EN_PREPARACION);
            p.setRegistradoPor(actual.get());
        }
        p = repo.save(p);
        historial.registrar("Propuesta", p.getId(), null, nuevo ? "CREACION" : "MODIFICACION",
                (nuevo ? "Se registró la propuesta " : "Se modificó la propuesta ") + p.getCodigo() + " por S/ " + p.getMonto());
        return p;
    }

    @Transactional
    public void cambiarEstado(Long id, EstadoPropuesta nuevo) {
        Propuesta p = obtener(id);
        if (nuevo == null || !TRANSICIONES.get(p.getEstado()).contains(nuevo)) {
            throw new ReglaNegocioException("No se puede pasar la propuesta de «" + p.getEstado().getEtiqueta()
                    + "» a «" + (nuevo == null ? "-" : nuevo.getEtiqueta()) + "».");
        }
        if (nuevo == EstadoPropuesta.PENDIENTE_RESPUESTA) {
            List<String> faltan = documentos.obligatoriosFaltantes(p.getId());
            if (!faltan.isEmpty()) {
                throw new ReglaNegocioException("Faltan documentos obligatorios para presentar la propuesta: "
                        + String.join(", ", faltan) + ".");
            }
            if (p.getFechaEnvio() == null) {
                p.setFechaEnvio(LocalDate.now());
            }
        }
        EstadoPropuesta anterior = p.getEstado();
        p.setEstado(nuevo);
        repo.save(p);
        historial.registrar("Propuesta", p.getId(), null, "CAMBIO_ESTADO",
                "La propuesta " + p.getCodigo() + " pasó de «" + anterior.getEtiqueta() + "» a «" + nuevo.getEtiqueta() + "».");
    }

    /** RF-06: registra la respuesta u observacion de la entidad. Una observacion deja la propuesta como observada. */
    @Transactional
    public RespuestaPropuesta registrarRespuesta(Long propuestaId, TipoRespuesta tipo, LocalDate fecha, String detalle) {
        Propuesta p = obtener(propuestaId);
        if (tipo == null) {
            throw new ReglaNegocioException("Seleccione si es una respuesta o una observación.");
        }
        if (fecha == null) {
            throw new ReglaNegocioException("Ingrese la fecha.");
        }
        if (detalle == null || detalle.isBlank()) {
            throw new ReglaNegocioException("Ingrese el detalle de la respuesta u observación.");
        }
        if (p.getEstado() == EstadoPropuesta.EN_PREPARACION) {
            throw new ReglaNegocioException("La propuesta aún no fue presentada a la entidad.");
        }
        RespuestaPropuesta r = new RespuestaPropuesta();
        r.setPropuesta(p);
        r.setTipo(tipo);
        r.setFecha(fecha);
        r.setDetalle(detalle.trim());
        r.setRegistradoPor(actual.get());
        r = respuestas.save(r);
        if (tipo == TipoRespuesta.OBSERVACION && p.getEstado() == EstadoPropuesta.PENDIENTE_RESPUESTA) {
            p.setEstado(EstadoPropuesta.OBSERVADA);
            repo.save(p);
        }
        historial.registrar("Propuesta", p.getId(), null, "CREACION",
                "Se registró una " + tipo.getEtiqueta().toLowerCase() + " de la entidad en la propuesta " + p.getCodigo() + ".");
        return r;
    }
}
