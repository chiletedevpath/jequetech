package pe.jequetech.config;

import jakarta.servlet.http.*;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import pe.jequetech.model.*;
import pe.jequetech.service.UsuarioService;

@Component
public class SesionInterceptor implements HandlerInterceptor {
  private final UsuarioService usuarios;

  public SesionInterceptor(UsuarioService usuarios) {
    this.usuarios = usuarios;
  }

  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
      throws Exception {
    res.setHeader("Cache-Control", "no-store");
    res.setHeader("X-Content-Type-Options", "nosniff");
    res.setHeader("X-Frame-Options", "DENY");
    HttpSession session = req.getSession();
    if (session.getAttribute("csrf") == null)
      session.setAttribute("csrf", UUID.randomUUID().toString());
    if ("POST".equals(req.getMethod())
        && !session.getAttribute("csrf").equals(req.getParameter("_csrf"))) {
      res.sendError(403);
      return false;
    }
    String path = req.getRequestURI();
    if (path.equals("/login")) return true;
    Long id = (Long) session.getAttribute("usuarioId");
    if (id == null) {
      res.sendRedirect(req.getContextPath() + "/login");
      return false;
    }
    Usuario u = usuarios.buscar(id);
    if (!usuarios.habilitado(u)) {
      session.invalidate();
      res.sendRedirect(req.getContextPath() + "/login");
      return false;
    }
    req.setAttribute("actor", u);
    return true;
  }
}
