package pe.dracor.sgsao.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import pe.dracor.sgsao.domain.OrigenRequerimiento;
import pe.dracor.sgsao.domain.Requerimiento;
import pe.dracor.sgsao.service.ClienteService;
import pe.dracor.sgsao.service.DocumentoService;
import pe.dracor.sgsao.service.PropuestaService;
import pe.dracor.sgsao.service.ReglaNegocioException;
import pe.dracor.sgsao.service.RequerimientoService;

/** RF-04: requerimientos de clientes. */
@Controller
@RequestMapping("/requerimientos")
public class RequerimientoController {

    private final RequerimientoService service;
    private final ClienteService clientes;
    private final PropuestaService propuestas;
    private final DocumentoService documentos;

    public RequerimientoController(RequerimientoService service, ClienteService clientes,
                                   PropuestaService propuestas, DocumentoService documentos) {
        this.service = service;
        this.clientes = clientes;
        this.propuestas = propuestas;
        this.documentos = documentos;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("requerimientos", service.listar());
        return "requerimientos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Requerimiento r = new Requerimiento();
        r.setFechaRecepcion(LocalDate.now());
        return formulario(model, r);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("req", service.obtener(id));
        model.addAttribute("propuestas", propuestas.porRequerimiento(id));
        model.addAttribute("documentos", documentos.deRequerimiento(id));
        model.addAttribute("tipos", documentos.tiposActivos());
        return "requerimientos/detalle";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, service.obtener(id));
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("requerimiento") Requerimiento form, Model model, RedirectAttributes ra) {
        try {
            Requerimiento r = service.guardar(form);
            ra.addFlashAttribute("ok", "Requerimiento guardado correctamente.");
            return "redirect:/requerimientos/" + r.getId();
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, form);
        }
    }

    private String formulario(Model model, Requerimiento r) {
        model.addAttribute("requerimiento", r);
        model.addAttribute("clientes", clientes.activos());
        model.addAttribute("origenes", OrigenRequerimiento.values());
        return "requerimientos/form";
    }
}
