package pe.dracor.sgsao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import pe.dracor.sgsao.service.IntentosLoginService;

/**
 * Seguridad: autenticacion con formulario, contrasenas con BCrypt (RNF-03),
 * autorizacion por perfil (RN-05, RNF-04) y bloqueo temporal por intentos fallidos (RNF-05).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADM = "ADMINISTRADOR";
    private static final String JEF = "JEFE_PROYECTO";
    private static final String SUP = "SUPERVISOR";
    private static final String ADT = "ADMINISTRATIVO";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, IntentosLoginService intentos) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/img/**", "/js/**", "/webjars/**", "/error").permitAll()
                .requestMatchers("/usuarios/**", "/tipos-documento/**").hasRole(ADM)
                .requestMatchers("/auditoria/**").hasAnyRole(ADM, JEF)
                .requestMatchers("/clientes/**", "/requerimientos/**", "/propuestas/**").hasAnyRole(ADM, ADT, JEF)
                .requestMatchers("/obras/nueva", "/obras/guardar", "/obras/*/editar", "/obras/*/cerrar",
                        "/obras/*/estado", "/actividades/**").hasAnyRole(ADM, JEF)
                .requestMatchers("/avances/**", "/incidencias/**", "/evidencias/**").hasAnyRole(ADM, JEF, SUP)
                .requestMatchers("/documentos/**").hasAnyRole(ADM, ADT, JEF, SUP)
                .requestMatchers("/reportes/obras/**").hasAnyRole(ADM, JEF)
                .requestMatchers("/reportes/**").hasAnyRole(ADM, ADT, JEF)
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    intentos.registrarExito(authentication.getName());
                    response.sendRedirect(request.getContextPath() + "/");
                })
                .failureHandler((request, response, exception) -> {
                    String motivo = "credenciales";
                    if (exception instanceof DisabledException) {
                        motivo = "inactivo";
                    } else if (exception instanceof LockedException) {
                        motivo = "bloqueado";
                    } else if (exception instanceof BadCredentialsException) {
                        boolean bloqueado = intentos.registrarFallo(request.getParameter("username"));
                        if (bloqueado) {
                            motivo = "bloqueado";
                        }
                    }
                    response.sendRedirect(request.getContextPath() + "/login?error=" + motivo);
                })
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"));
        return http.build();
    }
}
