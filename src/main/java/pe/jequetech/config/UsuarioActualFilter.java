package pe.jequetech.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;
import pe.jequetech.model.Usuario;
import pe.jequetech.service.UsuarioService;

/** Revalida la cuenta y su rol en MySQL antes de autorizar cada petición. */
final class UsuarioActualFilter extends OncePerRequestFilter {
  private final UsuarioService usuarios;

  UsuarioActualFilter(UsuarioService usuarios) { this.usuarios = usuarios; }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    var autenticacion = SecurityContextHolder.getContext().getAuthentication();
    if (autenticacion != null && autenticacion.getPrincipal() instanceof Long id) {
      Usuario usuario = null;
      try { usuario = usuarios.buscar(id); }
      catch (ResponseStatusException ex) {
        if (ex.getStatusCode().value() != 404) throw ex;
      }
      if (usuario == null || !usuarios.habilitado(usuario)) {
        SecurityContextHolder.clearContext();
        var sesion = req.getSession(false);
        if (sesion != null) sesion.invalidate();
        res.sendRedirect(req.getContextPath() + "/login");
        return;
      }
      var contexto = SecurityContextHolder.createEmptyContext();
      contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(id, null,
          List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().name()))));
      SecurityContextHolder.setContext(contexto);
      req.setAttribute("actor", usuario);
    }
    chain.doFilter(req, res);
  }
}
