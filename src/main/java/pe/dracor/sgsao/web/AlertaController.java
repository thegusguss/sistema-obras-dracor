package pe.dracor.sgsao.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pe.dracor.sgsao.service.AlertaService;

/** RF-16: alertas de actividades vencidas y por vencer. */
@Controller
@RequestMapping("/alertas")
public class AlertaController {

    private final AlertaService service;

    public AlertaController(AlertaService service) {
        this.service = service;
    }

    @GetMapping
    public String alertas(Model model) {
        model.addAttribute("vencidas", service.vencidas());
        model.addAttribute("porVencer", service.porVencer());
        model.addAttribute("dias", service.getDiasPrevios());
        return "alertas";
    }
}
