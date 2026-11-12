package pe.dracor.sgsao.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.domain.Actividad;
import pe.dracor.sgsao.domain.EstadoIncidencia;
import pe.dracor.sgsao.domain.EstadoObra;
import pe.dracor.sgsao.domain.EstadoPropuesta;
import pe.dracor.sgsao.domain.Obra;
import pe.dracor.sgsao.domain.OrigenObra;
import pe.dracor.sgsao.domain.Propuesta;
import pe.dracor.sgsao.repository.ActividadRepository;
import pe.dracor.sgsao.repository.ClienteRepository;
import pe.dracor.sgsao.repository.IncidenciaRepository;
import pe.dracor.sgsao.repository.ObraRepository;
import pe.dracor.sgsao.repository.PropuestaRepository;
import pe.dracor.sgsao.repository.UsuarioRepository;

/** RF-09, RF-12 y RF-17: obras o servicios, avance de la obra, cierre y conformidad. */
@Service
public class ObraService {

    private static final Map<EstadoObra, Set<EstadoObra>> TRANSICIONES = new EnumMap<>(EstadoObra.class);

    static {
        TRANSICIONES.put(EstadoObra.PLANIFICADA, Set.of(EstadoObra.EN_EJECUCION));
        TRANSICIONES.put(EstadoObra.EN_EJECUCION, Set.of(EstadoObra.PARALIZADA, EstadoObra.FINALIZADA));
        TRANSICIONES.put(EstadoObra.PARALIZADA, Set.of(EstadoObra.EN_EJECUCION));
        TRANSICIONES.put(EstadoObra.FINALIZADA, Set.of(EstadoObra.EN_EJECUCION));
        TRANSICIONES.put(EstadoObra.CERRADA, Set.of());
    }

    private final ObraRepository repo;
    private final ActividadRepository actividades;
    private final IncidenciaRepository incidencias;
    private final ClienteRepository clientes;
    private final PropuestaRepository propuestas;
    private final UsuarioRepository usuarios;
    private final DocumentoService documentos;
    private final HistorialService historial;

    public ObraService(ObraRepository repo, ActividadRepository actividades, IncidenciaRepository incidencias,
                       ClienteRepository clientes, PropuestaRepository propuestas, UsuarioRepository usuarios,
                       DocumentoService documentos, HistorialService historial) {
        this.repo = repo;
        this.actividades = actividades;
        this.incidencias = incidencias;
        this.clientes = clientes;
        this.propuestas = propuestas;
        this.usuarios = usuarios;
        this.documentos = documentos;
        this.historial = historial;
    }

    // ------------------------------------------------------------------ consultas
    @Transactional(readOnly = true)
    public Obra obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new ReglaNegocioException("La obra no existe."));
    }

    @Transactional(readOnly = true)
    public List<Obra> todas() {
        return repo.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public List<ObraResumen> resumenes() {
        List<ObraResumen> lista = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (Obra o : repo.findAllByOrderByIdDesc()) {
            lista.add(resumen(o, hoy));
        }
        return lista;
    }

    @Transactional(readOnly = true)
    public ObraResumen resumen(Long obraId) {
        return resumen(obtener(obraId), LocalDate.now());
    }

    private ObraResumen resumen(Obra o, LocalDate hoy) {
        List<Actividad> acts = actividades.findByObraIdOrderByFechaInicioAscIdAsc(o.getId());
        int terminadas = 0;
        int vencidas = 0;
        for (Actividad a : acts) {
            if (a.getAvance() >= 100) {
                terminadas++;
            } else if (a.getFechaFin().isBefore(hoy) && !a.isJustificada()) {
                vencidas++;
            }
        }
        boolean activa = o.getEstado() == EstadoObra.PLANIFICADA || o.getEstado() == EstadoObra.EN_EJECUCION;
        long abiertas = incidencias.countByObraIdAndEstado(o.getId(), EstadoIncidencia.ABIERTA);
        return new ObraResumen(o, promedio(acts), acts.size(), terminadas, vencidas, abiertas, activa && vencidas > 0);
    }

    @Transactional(readOnly = true)
    public String sugerirCodigo() {
        int anio = LocalDate.now().getYear();
        long n = repo.count() + 1;
        String codigo = String.format("OB-%d-%03d", anio, n);
        while (repo.existsByCodigo(codigo)) {
            n++;
            codigo = String.format("OB-%d-%03d", anio, n);
        }
        return codigo;
    }

    @Transactional(readOnly = true)
    public Set<EstadoObra> siguientesEstados(Obra o) {
        return TRANSICIONES.get(o.getEstado());
    }

    // ------------------------------------------------------------------ RF-09
    @Transactional
    public Obra guardar(Obra form) {
        boolean nuevo = form.getId() == null;
        String codigo = form.getCodigo() == null ? "" : form.getCodigo().trim();
        if (codigo.isEmpty()) {
            throw new ReglaNegocioException("Ingrese el código de la obra.");
        }
        if (form.getNombre() == null || form.getNombre().isBlank()) {
            throw new ReglaNegocioException("Ingrese el nombre de la obra o servicio.");
        }
        if (form.getOrigen() == null) {
            throw new ReglaNegocioException("Seleccione el origen de la obra.");
        }
        if (form.getFechaInicio() == null || form.getFechaFin() == null) {
            throw new ReglaNegocioException("Ingrese las fechas previstas de inicio y fin.");
        }
        if (form.getFechaFin().isBefore(form.getFechaInicio())) {
            throw new ReglaNegocioException("La fecha de fin prevista no puede ser anterior a la de inicio.");
        }
        boolean duplicado = nuevo ? repo.existsByCodigo(codigo) : repo.existsByCodigoAndIdNot(codigo, form.getId());
        if (duplicado) {
            throw new ReglaNegocioException("Ya existe una obra con el código " + codigo + ".");
        }
        Obra o = nuevo ? new Obra() : obtener(form.getId());
        if (!nuevo && o.getEstado() == EstadoObra.CERRADA) {
            throw new ReglaNegocioException("La obra está cerrada y no se puede modificar.");
        }
        // RN-07: la obra nace de una propuesta aceptada o de una orden de servicio / contrato
        Propuesta propuesta = null;
        if (form.getOrigen() == OrigenObra.PROPUESTA_ACEPTADA) {
            if (form.getPropuesta() == null || form.getPropuesta().getId() == null) {
                throw new ReglaNegocioException("Seleccione la propuesta aceptada que origina la obra.");
            }
            propuesta = propuestas.findById(form.getPropuesta().getId())
                    .orElseThrow(() -> new ReglaNegocioException("La propuesta no existe."));
            if (propuesta.getEstado() != EstadoPropuesta.ACEPTADA) {
                throw new ReglaNegocioException("Solo una propuesta aceptada puede convertirse en obra.");
            }
            boolean yaUsada = nuevo ? repo.existsByPropuestaId(propuesta.getId())
                    : (o.getPropuesta() == null || !o.getPropuesta().getId().equals(propuesta.getId())) && repo.existsByPropuestaId(propuesta.getId());
            if (yaUsada) {
                throw new ReglaNegocioException("La propuesta " + propuesta.getCodigo() + " ya originó otra obra.");
            }
        } else if (form.getNroOrdenContrato() == null || form.getNroOrdenContrato().isBlank()) {
            throw new ReglaNegocioException("Ingrese el número de la orden de servicio o del contrato.");
        }
        Long clienteId = propuesta != null ? propuesta.getRequerimiento().getCliente().getId()
                : (form.getCliente() == null ? null : form.getCliente().getId());
        if (clienteId == null) {
            throw new ReglaNegocioException("Seleccione el cliente.");
        }
        if (!nuevo) {
            for (Actividad a : actividades.findByObraIdOrderByFechaInicioAscIdAsc(o.getId())) {
                if (a.getFechaInicio().isBefore(form.getFechaInicio()) || a.getFechaFin().isAfter(form.getFechaFin())) {
                    throw new ReglaNegocioException("La actividad «" + a.getNombre()
                            + "» quedaría fuera del nuevo plazo de la obra. Ajuste primero sus fechas.");
                }
            }
        }
        o.setCodigo(codigo);
        o.setNombre(form.getNombre().trim());
        o.setCliente(clientes.findById(clienteId).orElseThrow(() -> new ReglaNegocioException("El cliente no existe.")));
        o.setPropuesta(propuesta);
        o.setOrigen(form.getOrigen());
        o.setNroOrdenContrato(propuesta != null ? null : form.getNroOrdenContrato().trim());
        o.setUbicacion(form.getUbicacion() == null || form.getUbicacion().isBlank() ? null : form.getUbicacion().trim());
        o.setFechaInicio(form.getFechaInicio());
        o.setFechaFin(form.getFechaFin());
        o.setRequiereConformidad(form.isRequiereConformidad());
        o.setJefe(form.getJefe() != null && form.getJefe().getId() != null
                ? usuarios.findById(form.getJefe().getId()).orElse(null) : null);
        if (nuevo) {
            o.setEstado(EstadoObra.PLANIFICADA);
        }
        o = repo.save(o);
        historial.registrar("Obra", o.getId(), o.getId(), nuevo ? "CREACION" : "MODIFICACION",
                (nuevo ? "Se registró la obra " : "Se modificó la obra ") + o.getCodigo() + " - " + o.getNombre());
        return o;
    }

    @Transactional
    public void cambiarEstado(Long id, EstadoObra nuevo) {
        Obra o = obtener(id);
        if (nuevo == null || !TRANSICIONES.get(o.getEstado()).contains(nuevo)) {
            throw new ReglaNegocioException("No se puede pasar la obra de «" + o.getEstado().getEtiqueta()
                    + "» a «" + (nuevo == null ? "-" : nuevo.getEtiqueta()) + "».");
        }
        if (nuevo == EstadoObra.FINALIZADA) {
            List<String> pend = pendientesDeActividades(o);
            if (!pend.isEmpty()) {
                throw new ReglaNegocioException("No se puede finalizar la obra: " + String.join("; ", pend) + ".");
            }
        }
        EstadoObra anterior = o.getEstado();
        o.setEstado(nuevo);
        repo.save(o);
        historial.registrar("Obra", o.getId(), o.getId(), "CAMBIO_ESTADO",
                "La obra " + o.getCodigo() + " pasó de «" + anterior.getEtiqueta() + "» a «" + nuevo.getEtiqueta() + "».");
    }

    private List<String> pendientesDeActividades(Obra o) {
        List<String> pend = new ArrayList<>();
        List<Actividad> acts = actividades.findByObraIdOrderByFechaInicioAscIdAsc(o.getId());
        if (acts.isEmpty()) {
            pend.add("la obra no tiene actividades registradas");
        }
        int sin = 0;
        for (Actividad a : acts) {
            if (a.getAvance() < 100 && !a.isJustificada()) {
                sin++;
            }
        }
        if (sin > 0) {
            pend.add(sin + " actividad(es) sin terminar ni justificar");
        }
        return pend;
    }
}
