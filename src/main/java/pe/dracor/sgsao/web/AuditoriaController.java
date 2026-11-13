package pe.dracor.sgsao.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.dracor.sgsao.service.HistorialService;

/** RF-15: historial de actualizaciones. */
@Controller
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final HistorialService service;

    public AuditoriaController(HistorialService service) {
        this.service = service;
    }

    @GetMapping
    public String historial(@RequestParam(required = false) String entidad, Model model) {
        model.addAttribute("registros", service.ultimos(entidad));
        model.addAttribute("entidad", entidad);
        return "auditoria";
    }
}
