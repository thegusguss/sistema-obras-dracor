package pe.dracor.sgsao.web;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.dracor.sgsao.domain.Actividad;
import pe.dracor.sgsao.service.ActividadService;
import pe.dracor.sgsao.service.AvanceService;
import pe.dracor.sgsao.service.ReglaNegocioException;

/** RF-11: registro del avance de actividades. */
@Controller
@RequestMapping("/avances")
public class AvanceController {

    private final AvanceService service;
    private final ActividadService actividades;

    public AvanceController(AvanceService service, ActividadService actividades) {
        this.service = service;
        this.actividades = actividades;
    }

    @GetMapping("/actividad/{id}")
    public String formulario(@PathVariable Long id, Model model) {
        Actividad a = actividades.obtener(id);
        model.addAttribute("actividad", a);
        model.addAttribute("avances", service.deActividad(id));
        model.addAttribute("hoy", LocalDate.now());
        return "avances/form";
    }

    @PostMapping("/actividad/{id}")
    public String registrar(@PathVariable Long id, @RequestParam Integer porcentaje,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                            @RequestParam(required = false) String observacion,
                            @RequestParam(required = false) String motivoRetroceso,
                            Model model, RedirectAttributes ra) {
        Actividad a = actividades.obtener(id);
        try {
            service.registrar(id, porcentaje, fecha, observacion, motivoRetroceso);
            ra.addFlashAttribute("ok", "Avance registrado correctamente.");
            return "redirect:/obras/" + a.getObra().getId();
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("actividad", a);
            model.addAttribute("avances", service.deActividad(id));
            model.addAttribute("hoy", LocalDate.now());
            return "avances/form";
        }
    }
}
