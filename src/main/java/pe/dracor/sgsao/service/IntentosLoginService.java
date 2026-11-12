package pe.dracor.sgsao.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.repository.UsuarioRepository;

/** RNF-05: bloqueo temporal despues de varios intentos fallidos de ingreso. */
@Service
public class IntentosLoginService {

    private final UsuarioRepository usuarios;
    private final int maxIntentos;
    private final int minutosBloqueo;

    public IntentosLoginService(UsuarioRepository usuarios,
                                @Value("${dracor.seguridad.max-intentos:5}") int maxIntentos,
                                @Value("${dracor.seguridad.minutos-bloqueo:10}") int minutosBloqueo) {
        this.usuarios = usuarios;
        this.maxIntentos = maxIntentos;
        this.minutosBloqueo = minutosBloqueo;
    }

    /** Devuelve true si, con este intento, el usuario queda bloqueado. */
    @Transactional
    public boolean registrarFallo(String username) {
        if (username == null) {
            return false;
        }
        return usuarios.findByUsername(username).map(u -> {
            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            if (u.getIntentosFallidos() >= maxIntentos) {
                u.setBloqueadoHasta(LocalDateTime.now().plusMinutes(minutosBloqueo));
                u.setIntentosFallidos(0);
                usuarios.save(u);
                return true;
            }
            usuarios.save(u);
            return false;
        }).orElse(false);
    }

    @Transactional
    public void registrarExito(String username) {
        usuarios.findByUsername(username).ifPresent(u -> {
            u.setIntentosFallidos(0);
            u.setBloqueadoHasta(null);
            usuarios.save(u);
        });
    }
}
