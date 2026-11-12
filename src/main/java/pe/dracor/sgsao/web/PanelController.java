package pe.dracor.sgsao.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.dracor.sgsao.service.DashboardService;

/** RF-18: panel de obras. */
@Controller
public class PanelController {

    private final DashboardService dashboard;

    public PanelController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping({"/", "/panel"})
    public String panel(Model model) {
        model.addAttribute("panel", dashboard.resumen());
        return "panel";
    }
}
