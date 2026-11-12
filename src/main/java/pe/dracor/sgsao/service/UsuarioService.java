package pe.dracor.sgsao.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.domain.Rol;
import pe.dracor.sgsao.domain.Usuario;
import pe.dracor.sgsao.repository.UsuarioRepository;

/** RF-02: gestion de usuarios y perfiles (RN-02, RN-03, RN-04, RN-05). */
@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final HistorialService historial;
    private final UsuarioActual actual;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder, HistorialService historial, UsuarioActual actual) {
        this.repo = repo;
        this.encoder = encoder;
        this.historial = historial;
        this.actual = actual;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repo.findAllByOrderByApellidosAscNombresAsc();
    }

    @Transactional(readOnly = true)
    public List<Usuario> activos() {
        return repo.findByActivoTrueOrderByApellidosAscNombresAsc();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new ReglaNegocioException("El usuario no existe."));
    }

    @Transactional
    public Usuario guardar(Usuario form, String passwordPlano, Set<Rol> roles) {
        boolean nuevo = form.getId() == null;
        String username = form.getUsername() == null ? "" : form.getUsername().trim();
        if (blank(form.getNombres()) || blank(form.getApellidos())) {
            throw new ReglaNegocioException("Ingrese los nombres y apellidos del usuario.");
        }
        if (username.length() < 4) {
            throw new ReglaNegocioException("El nombre de usuario debe tener al menos 4 caracteres.");
        }
        if (!blank(form.getDni()) && !form.getDni().trim().matches("\\d{8}")) {
            throw new ReglaNegocioException("El DNI debe tener exactamente 8 dígitos.");
        }
        if (roles == null || roles.isEmpty()) {
            throw new ReglaNegocioException("Asigne al menos un perfil al usuario.");
        }
        boolean duplicado = nuevo ? repo.existsByUsername(username) : repo.existsByUsernameAndIdNot(username, form.getId());
        if (duplicado) {
            throw new ReglaNegocioException("Ya existe un usuario con el nombre «" + username + "».");
        }
        boolean cambiaClave = !blank(passwordPlano);
        if (nuevo && !cambiaClave) {
            throw new ReglaNegocioException("Ingrese una contraseña para el nuevo usuario.");
        }
        if (cambiaClave && passwordPlano.length() < 8) {
            throw new ReglaNegocioException("La contraseña debe tener al menos 8 caracteres.");
        }
        Usuario u = nuevo ? new Usuario() : obtener(form.getId());
        u.setNombres(form.getNombres().trim());
        u.setApellidos(form.getApellidos().trim());
        u.setDni(blank(form.getDni()) ? null : form.getDni().trim());
        u.setUsername(username);
        u.setRoles(new HashSet<>(roles));
        if (nuevo) {
            u.setActivo(true);
        }
        if (cambiaClave) {
            u.setPassword(encoder.encode(passwordPlano));
        }
        u = repo.save(u);
        historial.registrar("Usuario", u.getId(), null, nuevo ? "CREACION" : "MODIFICACION",
                (nuevo ? "Se creó el usuario " : "Se modificó el usuario ") + u.getUsername() + " con perfiles: " + u.getRolesTexto());
        return u;
    }

    @Transactional
    public void cambiarEstado(Long id) {
        Usuario u = obtener(id);
        Usuario yo = actual.get();
        if (yo != null && yo.getId().equals(u.getId())) {
            throw new ReglaNegocioException("No puede desactivar su propio usuario.");
        }
        u.setActivo(!u.isActivo());
        if (u.isActivo()) {
            u.setBloqueadoHasta(null);
            u.setIntentosFallidos(0);
        }
        repo.save(u);
        historial.registrar("Usuario", u.getId(), null, "MODIFICACION",
                "El usuario " + u.getUsername() + (u.isActivo() ? " fue activado." : " fue desactivado."));
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
