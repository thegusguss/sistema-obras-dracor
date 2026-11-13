package pe.dracor.sgsao.web;

import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.dracor.sgsao.domain.Documento;
import pe.dracor.sgsao.domain.TipoDocumento;
import pe.dracor.sgsao.service.DocumentoService;
import pe.dracor.sgsao.service.ReglaNegocioException;
import pe.dracor.sgsao.util.Redireccion;

/** RF-07 y RF-08: documentos y tipos documentales. */
@Controller
public class DocumentoController {

    private final DocumentoService service;

    public DocumentoController(DocumentoService service) {
        this.service = service;
    }

    // ---------- Documentos ----------
    @GetMapping("/documentos")
    public String lista(Model model) {
        model.addAttribute("documentos", service.todos());
        return "documentos/lista";
    }

    @PostMapping("/documentos/subir")
    public String subir(@RequestParam("archivo") MultipartFile archivo, @RequestParam Long tipoId,
                        @RequestParam(required = false) String nombre,
                        @RequestParam(required = false) Long requerimientoId,
                        @RequestParam(required = false) Long propuestaId,
                        @RequestParam(required = false) Long obraId,
                        @RequestParam(required = false) String volver, RedirectAttributes ra) {
        try {
            service.subir(archivo, tipoId, nombre, requerimientoId, propuestaId, obraId);
            ra.addFlashAttribute("ok", "Documento cargado correctamente.");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return Redireccion.seguro(volver, "/documentos");
    }

    @PostMapping("/documentos/{id}/anular")
    public String anular(@PathVariable Long id, @RequestParam String motivo,
                         @RequestParam(required = false) String volver, RedirectAttributes ra) {
        try {
            service.anular(id, motivo);
            ra.addFlashAttribute("ok", "Documento anulado. Queda en el historial.");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return Redireccion.seguro(volver, "/documentos");
    }

    @GetMapping("/documentos/{id}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable Long id) {
        Documento d = service.obtener(id);
        Resource r = service.recurso(d);
        return respuesta(r, d.getArchivoOriginal(), d.getContentType());
    }

    // ---------- Tipos documentales ----------
    @GetMapping("/tipos-documento")
    public String tipos(Model model) {
        model.addAttribute("tipos", service.todosLosTipos());
        return "documentos/tipos";
    }

    @GetMapping("/tipos-documento/nuevo")
    public String nuevoTipo(Model model) {
        model.addAttribute("tipo", new TipoDocumento());
        return "documentos/tipo-form";
    }

    @GetMapping("/tipos-documento/{id}/editar")
    public String editarTipo(@PathVariable Long id, Model model) {
        model.addAttribute("tipo", service.obtenerTipo(id));
        return "documentos/tipo-form";
    }

    @PostMapping("/tipos-documento/guardar")
    public String guardarTipo(@ModelAttribute("tipo") TipoDocumento form, Model model, RedirectAttributes ra) {
        try {
            service.guardarTipo(form);
            ra.addFlashAttribute("ok", "Tipo documental guardado.");
            return "redirect:/tipos-documento";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return "documentos/tipo-form";
        }
    }

    @PostMapping("/tipos-documento/{id}/estado")
    public String estadoTipo(@PathVariable Long id, RedirectAttributes ra) {
        service.cambiarEstadoTipo(id);
        ra.addFlashAttribute("ok", "Estado del tipo documental actualizado.");
        return "redirect:/tipos-documento";
    }

    static ResponseEntity<Resource> respuesta(Resource r, String nombre, String contentType) {
        MediaType tipo;
        try {
            tipo = contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
        } catch (Exception e) {
            tipo = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(nombre, StandardCharsets.UTF_8).build().toString())
                .body(r);
    }
}
