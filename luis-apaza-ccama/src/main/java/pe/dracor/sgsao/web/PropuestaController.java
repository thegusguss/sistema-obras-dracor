package pe.dracor.sgsao.web;

import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.dracor.sgsao.domain.EstadoPropuesta;
import pe.dracor.sgsao.domain.Propuesta;
import pe.dracor.sgsao.domain.Requerimiento;
import pe.dracor.sgsao.domain.TipoRespuesta;
import pe.dracor.sgsao.service.DocumentoService;
import pe.dracor.sgsao.service.PropuestaService;
import pe.dracor.sgsao.service.ReglaNegocioException;
import pe.dracor.sgsao.service.RequerimientoService;

/** RF-05 y RF-06: propuestas, cotizaciones, respuestas y observaciones. */
@Controller
@RequestMapping("/propuestas")
public class PropuestaController {

    private final PropuestaService service;
    private final RequerimientoService requerimientos;
    private final DocumentoService documentos;

    public PropuestaController(PropuestaService service, RequerimientoService requerimientos,
                               DocumentoService documentos) {
        this.service = service;
        this.requerimientos = requerimientos;
        this.documentos = documentos;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) EstadoPropuesta estado, Model model) {
        model.addAttribute("propuestas", service.listar(estado));
        model.addAttribute("estado", estado);
        model.addAttribute("estados", EstadoPropuesta.values());
        return "propuestas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long requerimientoId, Model model) {
        Propuesta p = new Propuesta();
        if (requerimientoId != null) {
            p.setRequerimiento(requerimientos.obtener(requerimientoId));
        }
        return formulario(model, p);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Propuesta p = service.obtener(id);
        model.addAttribute("p", p);
        model.addAttribute("respuestas", service.respuestasDe(id));
        model.addAttribute("documentos", documentos.dePropuesta(id));
        model.addAttribute("tipos", documentos.tiposActivos());
        model.addAttribute("faltantes", documentos.obligatoriosFaltantes(id));
        model.addAttribute("siguientes", service.siguientesEstados(p));
        model.addAttribute("tiposRespuesta", TipoRespuesta.values());
        model.addAttribute("hoy", LocalDate.now());
        return "propuestas/detalle";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, service.obtener(id));
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("propuesta") Propuesta form, Model model, RedirectAttributes ra) {
        try {
            Propuesta p = service.guardar(form);
            ra.addFlashAttribute("ok", "Propuesta " + p.getCodigo() + " guardada correctamente.");
            return "redirect:/propuestas/" + p.getId();
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, form);
        }
    }

    @PostMapping("/{id}/estado")
    public String estado(@PathVariable Long id, @RequestParam EstadoPropuesta nuevo, RedirectAttributes ra) {
        try {
            service.cambiarEstado(id, nuevo);
            ra.addFlashAttribute("ok", "La propuesta pasó a «" + nuevo.getEtiqueta() + "».");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/propuestas/" + id;
    }

    @PostMapping("/{id}/respuesta")
    public String respuesta(@PathVariable Long id, @RequestParam TipoRespuesta tipo,
                            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fecha,
                            @RequestParam(required = false) String detalle, RedirectAttributes ra) {
        try {
            service.registrarRespuesta(id, tipo, fecha, detalle);
            ra.addFlashAttribute("ok", "Respuesta registrada.");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/propuestas/" + id;
    }

    private String formulario(Model model, Propuesta p) {
        model.addAttribute("propuesta", p);
        model.addAttribute("requerimientos", requerimientos.listar());
        return "propuestas/form";
    }
}
