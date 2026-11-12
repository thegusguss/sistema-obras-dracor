package pe.dracor.sgsao.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pe.dracor.sgsao.domain.Usuario;
import pe.dracor.sgsao.repository.UsuarioRepository;

/** Entrega el usuario que tiene la sesion iniciada. */
@Component
public class UsuarioActual {

    private final UsuarioRepository usuarios;

    public UsuarioActual(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return usuarios.findByUsername(auth.getName()).orElse(null);
    }

    public String nombre() {
        Usuario u = get();
        return u == null ? "sistema" : u.getUsername();
    }
}
