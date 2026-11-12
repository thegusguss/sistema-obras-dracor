package pe.dracor.sgsao.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.dracor.sgsao.domain.Usuario;
import pe.dracor.sgsao.service.ReglaNegocioException;
import pe.dracor.sgsao.service.UsuarioActual;

/** Datos comunes de todas las pantallas y manejo de errores. */
@ControllerAdvice
public class GlobalModel {

    private final UsuarioActual actual;

    public GlobalModel(UsuarioActual actual) {
        this.actual = actual;
    }

    @ModelAttribute("yo")
    public Usuario yo() {
        return actual.get();
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public String reglaDeNegocio(ReglaNegocioException e, Model model) {
        model.addAttribute("titulo", "No se pudo completar la operación");
        model.addAttribute("mensaje", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String errorGeneral(Exception e, Model model) {
        model.addAttribute("titulo", "Ocurrió un error inesperado");
        model.addAttribute("mensaje", "Intente nuevamente. Si el problema continúa, avise al administrador del sistema.");
        model.addAttribute("detalle", e.getClass().getSimpleName() + ": " + e.getMessage());
        return "error";
    }
}
