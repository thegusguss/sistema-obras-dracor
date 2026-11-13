package pe.dracor.sgsao.web;

import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.dracor.sgsao.domain.EstadoPropuesta;
import pe.dracor.sgsao.service.ObraService;
import pe.dracor.sgsao.service.PropuestaService;
import pe.dracor.sgsao.service.ReporteService;
import pe.dracor.sgsao.util.ExportadorExcel;
import pe.dracor.sgsao.util.ExportadorPdf;
import pe.dracor.sgsao.util.ReporteTabla;

/** RF-19 y RF-20: reportes con vista previa y exportación a PDF o Excel. */
@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReporteService service;
    private final ObraService obras;
    private final PropuestaService propuestas;

    public ReporteController(ReporteService service, ObraService obras, PropuestaService propuestas) {
        this.service = service;
        this.obras = obras;
        this.propuestas = propuestas;
    }

    @GetMapping
    public String indice() {
        return "reportes/indice";
    }

    // ---------- Obras ----------
    @GetMapping("/obras")
    public String obras(@RequestParam(defaultValue = "TODAS") String filtro, Model model) {
        model.addAttribute("tabla", service.obras(filtro));
        model.addAttribute("filtro", filtro);
        model.addAttribute("filtros", ReporteService.FILTROS_OBRAS);
        return "reportes/obras";
    }

    @GetMapping("/obras/pdf")
    public ResponseEntity<byte[]> obrasPdf(@RequestParam(defaultValue = "TODAS") String filtro) {
        return pdf(service.obras(filtro), "reporte_obras");
    }

    @GetMapping("/obras/excel")
    public ResponseEntity<byte[]> obrasExcel(@RequestParam(defaultValue = "TODAS") String filtro) {
        return excel(service.obras(filtro), "reporte_obras");
    }

    // ---------- Propuestas ----------
    @GetMapping("/propuestas")
    public String propuestas(@RequestParam(required = false) EstadoPropuesta estado, Model model) {
        model.addAttribute("tabla", service.propuestas(estado));
        model.addAttribute("estado", estado);
        model.addAttribute("estados", EstadoPropuesta.values());
        return "reportes/propuestas";
    }

    @GetMapping("/propuestas/pdf")
    public ResponseEntity<byte[]> propuestasPdf(@RequestParam(required = false) EstadoPropuesta estado) {
        return pdf(service.propuestas(estado), "reporte_propuestas");
    }

    @GetMapping("/propuestas/excel")
    public ResponseEntity<byte[]> propuestasExcel(@RequestParam(required = false) EstadoPropuesta estado) {
        return excel(service.propuestas(estado), "reporte_propuestas");
    }

    // ---------- Documentos ----------
    @GetMapping("/documentos")
    public String documentos(@RequestParam(required = false) Long obraId,
                             @RequestParam(required = false) Long propuestaId, Model model) {
        model.addAttribute("tabla", service.documentos(obraId, propuestaId));
        model.addAttribute("obraId", obraId);
        model.addAttribute("propuestaId", propuestaId);
        model.addAttribute("listaObras", obras.todas());
        model.addAttribute("listaPropuestas", propuestas.listar(null));
        return "reportes/documentos";
    }

    @GetMapping("/documentos/pdf")
    public ResponseEntity<byte[]> documentosPdf(@RequestParam(required = false) Long obraId,
                                                @RequestParam(required = false) Long propuestaId) {
        return pdf(service.documentos(obraId, propuestaId), "reporte_documentos");
    }

    @GetMapping("/documentos/excel")
    public ResponseEntity<byte[]> documentosExcel(@RequestParam(required = false) Long obraId,
                                                  @RequestParam(required = false) Long propuestaId) {
        return excel(service.documentos(obraId, propuestaId), "reporte_documentos");
    }

    private ResponseEntity<byte[]> pdf(ReporteTabla t, String nombre) {
        return descarga(ExportadorPdf.generar(t), MediaType.APPLICATION_PDF, nombre + ".pdf");
    }

    private ResponseEntity<byte[]> excel(ReporteTabla t, String nombre) {
        return descarga(ExportadorExcel.generar(t), XLSX, nombre + ".xlsx");
    }

    private ResponseEntity<byte[]> descarga(byte[] datos, MediaType tipo, String archivo) {
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo, StandardCharsets.UTF_8).build().toString())
                .body(datos);
    }
}
