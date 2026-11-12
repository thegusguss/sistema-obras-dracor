package pe.dracor.sgsao.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.dracor.sgsao.domain.Rol;
import pe.dracor.sgsao.domain.Usuario;
import pe.dracor.sgsao.repository.UsuarioRepository;

/** Carga el usuario de la base de datos para el inicio de sesion (RF-01, RN-06). */
@Service
public class DracorUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public DracorUserDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarios.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        List<GrantedAuthority> autoridades = new ArrayList<>();
        for (Rol r : u.getRoles()) {
            autoridades.add(new SimpleGrantedAuthority("ROLE_" + r.name()));
        }
        boolean bloqueado = u.getBloqueadoHasta() != null && u.getBloqueadoHasta().isAfter(LocalDateTime.now());
        return User.withUsername(u.getUsername())
                .password(u.getPassword())
                .disabled(!u.isActivo())
                .accountLocked(bloqueado)
                .authorities(autoridades)
                .build();
    }
}
