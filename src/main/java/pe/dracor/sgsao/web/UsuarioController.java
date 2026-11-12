package pe.dracor.sgsao.web;

import java.util.HashSet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.dracor.sgsao.domain.Rol;
import pe.dracor.sgsao.domain.Usuario;
import pe.dracor.sgsao.service.ReglaNegocioException;
import pe.dracor.sgsao.service.UsuarioService;

/** RF-02: usuarios y perfiles. */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("usuarios", service.listar());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Usuario u = new Usuario();
        u.setRoles(new HashSet<>());
        return formulario(model, u);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, service.obtener(id));
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("usuario") Usuario form, @RequestParam(required = false) String clave,
                          Model model, RedirectAttributes ra) {
        try {
            Usuario u = service.guardar(form, clave, form.getRoles());
            ra.addFlashAttribute("ok", "Usuario «" + u.getUsername() + "» guardado correctamente.");
            return "redirect:/usuarios";
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, form);
        }
    }

    @PostMapping("/{id}/estado")
    public String estado(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.cambiarEstado(id);
            ra.addFlashAttribute("ok", "Estado del usuario actualizado.");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    private String formulario(Model model, Usuario u) {
        model.addAttribute("usuario", u);
        model.addAttribute("todosRoles", Rol.values());
        return "usuarios/form";
    }
}
