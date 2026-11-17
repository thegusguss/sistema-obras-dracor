package pe.dracor.sgsao.web;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.dracor.sgsao.domain.Incidencia;
import pe.dracor.sgsao.service.IncidenciaService;
import pe.dracor.sgsao.service.ReglaNegocioException;

/** RF-13: incidencias de obra. */
@Controller
@RequestMapping("/incidencias")
public class IncidenciaController {

    private final IncidenciaService service;

    public IncidenciaController(IncidenciaService service) {
        this.service = service;
    }

    @PostMapping("/registrar")
    public String registrar(@RequestParam Long obraId, @ModelAttribute Incidencia form, RedirectAttributes ra) {
        try {
            service.registrar(obraId, form);
            ra.addFlashAttribute("ok", "Incidencia registrada.");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/obras/" + obraId;
    }

    @PostMapping("/{id}/resolver")
    public String resolver(@PathVariable Long id, @RequestParam Long obraId, @RequestParam String solucion,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                           RedirectAttributes ra) {
        try {
            service.resolver(id, solucion, fecha);
            ra.addFlashAttribute("ok", "Incidencia resuelta.");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/obras/" + obraId;
    }
}
