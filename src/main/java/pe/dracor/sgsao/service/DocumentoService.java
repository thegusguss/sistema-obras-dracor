package pe.dracor.sgsao.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.dracor.sgsao.domain.Documento;
import pe.dracor.sgsao.domain.TipoDocumento;
import pe.dracor.sgsao.repository.DocumentoRepository;
import pe.dracor.sgsao.repository.ObraRepository;
import pe.dracor.sgsao.repository.PropuestaRepository;
import pe.dracor.sgsao.repository.RequerimientoRepository;
import pe.dracor.sgsao.repository.TipoDocumentoRepository;

/** RF-07 y RF-08: documentos adjuntos y catalogo de tipos documentales. */
@Service
public class DocumentoService {

    private final DocumentoRepository repo;
    private final TipoDocumentoRepository tipos;
    private final RequerimientoRepository requerimientos;
    private final PropuestaRepository propuestas;
    private final ObraRepository obras;
    private final AlmacenArchivos almacen;
    private final HistorialService historial;
    private final UsuarioActual actual;

    public DocumentoService(DocumentoRepository repo, TipoDocumentoRepository tipos,
                            RequerimientoRepository requerimientos, PropuestaRepository propuestas,
                            ObraRepository obras, AlmacenArchivos almacen,
                            HistorialService historial, UsuarioActual actual) {
        this.repo = repo;
        this.tipos = tipos;
        this.requerimientos = requerimientos;
        this.propuestas = propuestas;
        this.obras = obras;
        this.almacen = almacen;
        this.historial = historial;
        this.actual = actual;
    }

    // ---------------------------------------------------------------- tipos documentales (RF-08)
    @Transactional(readOnly = true)
    public List<TipoDocumento> todosLosTipos() {
        return tipos.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<TipoDocumento> tiposActivos() {
        return tipos.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public TipoDocumento obtenerTipo(Long id) {
        return tipos.findById(id).orElseThrow(() -> new ReglaNegocioException("El tipo documental no existe."));
    }

    @Transactional
    public TipoDocumento guardarTipo(TipoDocumento form) {
        boolean nuevo = form.getId() == null;
        String nombre = form.getNombre() == null ? "" : form.getNombre().trim();
        if (nombre.isEmpty()) {
            throw new ReglaNegocioException("Ingrese el nombre del tipo documental.");
        }
        boolean duplicado = nuevo ? tipos.existsByNombreIgnoreCase(nombre) : tipos.existsByNombreIgnoreCaseAndIdNot(nombre, form.getId());
        if (duplicado) {
            throw new ReglaNegocioException("Ya existe un tipo documental llamado «" + nombre + "».");
        }
        TipoDocumento t = nuevo ? new TipoDocumento() : obtenerTipo(form.getId());
        t.setNombre(nombre);
        t.setObligatorio(form.isObligatorio());
        t.setConformidad(form.isConformidad());
        if (nuevo) {
            t.setActivo(true);
        }
        t = tipos.save(t);
        historial.registrar("TipoDocumento", t.getId(), null, nuevo ? "CREACION" : "MODIFICACION",
                (nuevo ? "Se creó" : "Se modificó") + " el tipo documental " + t.getNombre()
                        + (t.isObligatorio() ? " (obligatorio)" : ""));
        return t;
    }

    @Transactional
    public void cambiarEstadoTipo(Long id) {
        TipoDocumento t = obtenerTipo(id);
        t.setActivo(!t.isActivo());
        tipos.save(t);
        historial.registrar("TipoDocumento", t.getId(), null, "MODIFICACION",
                "El tipo documental " + t.getNombre() + (t.isActivo() ? " fue activado." : " fue desactivado."));
    }

    // ---------------------------------------------------------------- documentos (RF-07)
    @Transactional(readOnly = true)
    public List<Documento> todos() {
        return repo.findAllByOrderByFechaSubidaDesc();
    }

    @Transactional(readOnly = true)
    public List<Documento> deRequerimiento(Long id) {
        return repo.findByRequerimientoIdOrderByFechaSubidaDesc(id);
    }

    @Transactional(readOnly = true)
    public List<Documento> dePropuesta(Long id) {
        return repo.findByPropuestaIdOrderByFechaSubidaDesc(id);
    }

    @Transactional(readOnly = true)
    public List<Documento> deObra(Long id) {
        return repo.findByObraIdOrderByFechaSubidaDesc(id);
    }

    @Transactional(readOnly = true)
    public Documento obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new ReglaNegocioException("El documento no existe."));
    }

    @Transactional
    public Documento subir(MultipartFile archivo, Long tipoId, String nombre,
                           Long requerimientoId, Long propuestaId, Long obraId) {
        if (tipoId == null) {
            throw new ReglaNegocioException("Seleccione el tipo de documento.");
        }
        if (requerimientoId == null && propuestaId == null && obraId == null) {
            throw new ReglaNegocioException("El documento debe asociarse a un requerimiento, una propuesta o una obra.");
        }
        TipoDocumento tipo = obtenerTipo(tipoId);
        AlmacenArchivos.Guardado g = almacen.guardar(archivo);
        Documento d = new Documento();
        d.setTipo(tipo);
        d.setNombre(nombre == null || nombre.isBlank() ? g.original : nombre.trim());
        d.setArchivoOriginal(g.original);
        d.setArchivoGuardado(g.guardado);
        d.setContentType(g.contentType);
        d.setTamano(g.tamano);
        Long obraHistorial = null;
        if (requerimientoId != null) {
            d.setRequerimiento(requerimientos.findById(requerimientoId)
                    .orElseThrow(() -> new ReglaNegocioException("El requerimiento no existe.")));
        }
        if (propuestaId != null) {
            d.setPropuesta(propuestas.findById(propuestaId)
                    .orElseThrow(() -> new ReglaNegocioException("La propuesta no existe.")));
        }
        if (obraId != null) {
            d.setObra(obras.findById(obraId).orElseThrow(() -> new ReglaNegocioException("La obra no existe.")));
            obraHistorial = obraId;
        }
        d.setSubidoPor(actual.get());
        d = repo.save(d);
        historial.registrar("Documento", d.getId(), obraHistorial, "CREACION",
                "Se adjuntó el documento «" + d.getNombre() + "» (" + tipo.getNombre() + ") a " + d.getAsociadoA());
        return d;
    }

    @Transactional
    public void anular(Long id, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("Indique el motivo de la anulación.");
        }
        Documento d = obtener(id);
        d.setAnulado(true);
        d.setMotivoAnulacion(motivo.trim());
        repo.save(d);
        historial.registrar("Documento", d.getId(), d.getObra() == null ? null : d.getObra().getId(), "ANULACION",
                "Se anuló el documento «" + d.getNombre() + "». Motivo: " + d.getMotivoAnulacion());
    }

    public Resource recurso(Documento d) {
        return almacen.cargar(d.getArchivoGuardado());
    }

    /** RN-08: nombres de los documentos obligatorios que aun no tiene la propuesta. */
    @Transactional(readOnly = true)
    public List<String> obligatoriosFaltantes(Long propuestaId) {
        List<String> faltan = new ArrayList<>();
        for (TipoDocumento t : tipos.findByObligatorioTrueAndActivoTrue()) {
            if (!repo.existsByPropuestaIdAndTipoIdAndAnuladoFalse(propuestaId, t.getId())) {
                faltan.add(t.getNombre());
            }
        }
        return faltan;
    }
}
