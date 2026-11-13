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
import pe.dracor.sgsao.domain.Cliente;
import pe.dracor.sgsao.domain.TipoCliente;
import pe.dracor.sgsao.service.ClienteService;
import pe.dracor.sgsao.service.ReglaNegocioException;

/** RF-03: clientes y entidades. */
@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("clientes", service.listar(q));
        model.addAttribute("q", q);
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Cliente c = new Cliente();
        c.setTipo(TipoCliente.ENTIDAD_PUBLICA);
        return formulario(model, c);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, service.obtener(id));
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("cliente") Cliente form, Model model, RedirectAttributes ra) {
        try {
            Cliente c = service.guardar(form);
            ra.addFlashAttribute("ok", "Cliente «" + c.getRazonSocial() + "» guardado correctamente.");
            return "redirect:/clientes";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, form);
        }
    }

    @PostMapping("/{id}/estado")
    public String estado(@PathVariable Long id, RedirectAttributes ra) {
        service.cambiarEstado(id);
        ra.addFlashAttribute("ok", "Estado del cliente actualizado.");
        return "redirect:/clientes";
    }

    private String formulario(Model model, Cliente c) {
        model.addAttribute("cliente", c);
        model.addAttribute("tipos", TipoCliente.values());
        return "clientes/form";
    }
}
