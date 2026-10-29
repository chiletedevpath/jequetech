package pe.jequetech.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import pe.jequetech.service.UsuarioService;

@Configuration
public class SeguridadConfig {
  @Bean
  AuthenticationProvider autenticacionMySql(UsuarioService usuarios) {
    return new AuthenticationProvider() {
      @Override
      public Authentication authenticate(Authentication intento) {
        var usuario = usuarios.autenticar(intento.getName(), (String) intento.getCredentials())
            .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));
        return UsernamePasswordAuthenticationToken.authenticated(usuario.id(), null,
            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().name())));
      }

      @Override
      public boolean supports(Class<?> tipo) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(tipo);
      }
    };
  }

  @Bean
  SecurityFilterChain seguridad(HttpSecurity http, AuthenticationProvider autenticacionMySql,
      UsuarioService usuarios) throws Exception {
    http.authenticationProvider(autenticacionMySql)
        .authorizeHttpRequests(reglas -> reglas
            .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
            .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/vendor/**", "/favicon.ico").permitAll()
            .requestMatchers("/usuarios", "/usuarios/**", "/tecnicos", "/tecnicos/**").hasRole("ADMINISTRADOR")
            .requestMatchers("/clientes", "/clientes/**", "/equipos", "/equipos/**", "/seguimiento", "/seguimiento/**").hasRole("ADMINISTRATIVO")
            .anyRequest().authenticated())
        .formLogin(login -> login.loginPage("/login")
            .usernameParameter("correo").passwordParameter("clave")
            .defaultSuccessUrl("/dashboard", true).failureUrl("/login?error").permitAll())
        .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?salida")
            .invalidateHttpSession(true).deleteCookies("JSESSIONID"))
        .sessionManagement(sesion -> sesion.sessionFixation(fijacion -> fijacion.changeSessionId()))
        .requestCache(cache -> cache.requestCache(new NullRequestCache()))
        .exceptionHandling(errores -> errores.authenticationEntryPoint(
            (req, res, error) -> res.sendRedirect(req.getContextPath() + "/login")))
        .headers(headers -> headers.frameOptions(frame -> frame.deny()))
        .addFilterBefore(new UsuarioActualFilter(usuarios), AuthorizationFilter.class);
    return http.build();
  }
}
