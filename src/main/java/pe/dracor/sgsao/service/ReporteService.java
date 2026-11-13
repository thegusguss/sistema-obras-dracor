package pe.dracor.sgsao.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.domain.Documento;
import pe.dracor.sgsao.domain.EstadoObra;
import pe.dracor.sgsao.domain.EstadoPropuesta;
import pe.dracor.sgsao.domain.Propuesta;
import pe.dracor.sgsao.repository.DocumentoRepository;
import pe.dracor.sgsao.util.ReporteTabla;

/** RF-19 y RF-20: reportes de obras, propuestas y documentos. */
@Service
public class ReporteService {

    public static final String[][] FILTROS_OBRAS = {
        {"TODAS", "Todas las obras"},
        {"PENDIENTES", "Pendientes (planificadas)"},
        {"EN_EJECUCION", "En ejecución"},
        {"RETRASADAS", "Retrasadas"},
        {"FINALIZADAS", "Finalizadas y cerradas"}
    };

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ObraService obras;
    private final PropuestaService propuestas;
    private final DocumentoRepository documentos;

    public ReporteService(ObraService obras, PropuestaService propuestas, DocumentoRepository documentos) {
        this.obras = obras;
        this.propuestas = propuestas;
        this.documentos = documentos;
    }

    @Transactional(readOnly = true)
    public ReporteTabla obras(String filtro) {
        String f = filtro == null || filtro.isBlank() ? "TODAS" : filtro;
        String nombreFiltro = "Todas las obras";
        for (String[] par : FILTROS_OBRAS) {
            if (par[0].equals(f)) {
                nombreFiltro = par[1];
            }
        }
        ReporteTabla t = new ReporteTabla("Reporte de obras", "Filtro: " + nombreFiltro,
                Arrays.asList("Código", "Obra", "Cliente", "Estado", "Avance", "Actividades (term./total)",
                        "Vencidas", "Incidencias abiertas", "Inicio", "Fin previsto"));
        for (ObraResumen r : obras.resumenes()) {
            EstadoObra e = r.getObra().getEstado();
            boolean incluir;
            switch (f) {
                case "PENDIENTES":
                    incluir = e == EstadoObra.PLANIFICADA;
                    break;
                case "EN_EJECUCION":
                    incluir = e == EstadoObra.EN_EJECUCION;
                    break;
                case "RETRASADAS":
                    incluir = r.isRetrasada();
                    break;
                case "FINALIZADAS":
                    incluir = e == EstadoObra.FINALIZADA || e == EstadoObra.CERRADA;
                    break;
                default:
                    incluir = true;
            }
            if (!incluir) {
                continue;
            }
            t.agregar(Arrays.asList(r.getObra().getCodigo(), r.getObra().getNombre(),
                    r.getObra().getCliente().getRazonSocial(), r.getEstadoTexto(), r.getAvance() + " %",
                    r.getTerminadas() + " / " + r.getTotalActividades(), String.valueOf(r.getVencidas()),
                    String.valueOf(r.getIncidenciasAbiertas()), r.getObra().getFechaInicio().format(FECHA),
                    r.getObra().getFechaFin().format(FECHA)));
        }
        return t;
    }

    @Transactional(readOnly = true)
    public ReporteTabla propuestas(EstadoPropuesta estado) {
        ReporteTabla t = new ReporteTabla("Reporte de propuestas",
                "Estado: " + (estado == null ? "Todos" : estado.getEtiqueta()),
                Arrays.asList("Código", "Cliente", "Requerimiento", "Monto (S/)", "Fecha de envío", "Estado"));
        BigDecimal total = BigDecimal.ZERO;
        for (Propuesta p : propuestas.listar(estado)) {
            total = total.add(p.getMonto());
            t.agregar(Arrays.asList(p.getCodigo(), p.getRequerimiento().getCliente().getRazonSocial(),
                    resumirTexto(p.getRequerimiento().getDescripcion()), String.format("%,.2f", p.getMonto()),
                    p.getFechaEnvio() == null ? "-" : p.getFechaEnvio().format(FECHA), p.getEstado().getEtiqueta()));
        }
        if (!t.getFilas().isEmpty()) {
            t.agregar(Arrays.asList("", "", "TOTAL", String.format("%,.2f", total), "", ""));
        }
        return t;
    }

    @Transactional(readOnly = true)
    public ReporteTabla documentos(Long obraId, Long propuestaId) {
        List<Documento> lista;
        String sub;
        if (obraId != null) {
            lista = documentos.findByObraIdOrderByFechaSubidaDesc(obraId);
            sub = "Obra: " + obras.obtener(obraId).getCodigo();
        } else if (propuestaId != null) {
            lista = documentos.findByPropuestaIdOrderByFechaSubidaDesc(propuestaId);
            sub = "Propuesta: " + propuestas.obtener(propuestaId).getCodigo();
        } else {
            lista = documentos.findAllByOrderByFechaSubidaDesc();
            sub = "Todos los documentos";
        }
        ReporteTabla t = new ReporteTabla("Reporte de documentos", sub,
                Arrays.asList("Tipo documental", "Documento", "Asociado a", "Archivo", "Tamaño", "Subido por", "Fecha", "Situación"));
        for (Documento d : lista) {
            t.agregar(Arrays.asList(d.getTipo().getNombre(), d.getNombre(), d.getAsociadoA(), d.getArchivoOriginal(),
                    d.getTamanoTexto(), d.getSubidoPor() == null ? "-" : d.getSubidoPor().getUsername(),
                    d.getFechaSubida().format(FECHA_HORA), d.isAnulado() ? "Anulado" : "Vigente"));
        }
        return t;
    }

    private String resumirTexto(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > 90 ? s.substring(0, 87) + "..." : s;
    }
}
