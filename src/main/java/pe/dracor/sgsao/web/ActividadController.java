package pe.dracor.sgsao.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.dracor.sgsao.domain.Actividad;
import pe.dracor.sgsao.service.ActividadService;
import pe.dracor.sgsao.service.ObraService;
import pe.dracor.sgsao.service.ReglaNegocioException;
import pe.dracor.sgsao.service.UsuarioService;

/** RF-10: actividades y responsables. */
@Controller
@RequestMapping("/actividades")
public class ActividadController {

    private final ActividadService service;
    private final ObraService obras;
    private final UsuarioService usuarios;

    public ActividadController(ActividadService service, ObraService obras, UsuarioService usuarios) {
        this.service = service;
        this.obras = obras;
        this.usuarios = usuarios;
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam Long obraId, Model model) {
        Actividad a = new Actividad();
        a.setObra(obras.obtener(obraId));
        return formulario(model, a);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, service.obtener(id));
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("actividad") Actividad form, @RequestParam Long obraId,
                          Model model, RedirectAttributes ra) {
        try {
            service.guardar(form, obraId);
            ra.addFlashAttribute("ok", "Actividad guardada correctamente.");
            return "redirect:/obras/" + obraId;
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            form.setObra(obras.obtener(obraId));
            return formulario(model, form);
        }
    }

    @PostMapping("/{id}/reasignar")
    public String reasignar(@PathVariable Long id, @RequestParam Long obraId,
                            @RequestParam(required = false) Long responsableId, RedirectAttributes ra) {
        try {
            service.reasignar(id, responsableId);
            ra.addFlashAttribute("ok", "Responsable actualizado.");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/obras/" + obraId;
    }

    @PostMapping("/{id}/justificar")
    public String justificar(@PathVariable Long id, @RequestParam Long obraId, @RequestParam String texto,
                             RedirectAttributes ra) {
        try {
            service.justificar(id, texto);
            ra.addFlashAttribute("ok", "Justificación registrada.");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/obras/" + obraId;
    }

    private String formulario(Model model, Actividad a) {
        model.addAttribute("actividad", a);
        model.addAttribute("responsables", usuarios.activos());
        return "actividades/form";
    }
}
