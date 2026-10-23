package pe.jequetech.controller;

import jakarta.servlet.http.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.jequetech.model.*;
import pe.jequetech.service.*;

@ControllerAdvice
public class VistaGlobal {
  private final org.springframework.core.env.Environment environment;

  public VistaGlobal(org.springframework.core.env.Environment environment) {
    this.environment = environment;
  }

  @ModelAttribute
  public void comunes(HttpServletRequest req, Model model) {
    Usuario u = (Usuario) req.getAttribute("actor");
    model.addAttribute("actor", u);
    model.addAttribute("esAdmin", u != null && u.rol() == Rol.ADMINISTRADOR);
    model.addAttribute("esRecepcion", u != null && u.rol() == Rol.ADMINISTRATIVO);
    model.addAttribute("esTecnico", u != null && u.rol() == Rol.TECNICO);
    model.addAttribute("rutaActual", req.getRequestURI());
    model.addAttribute("estados", EstadoOrden.values());
    model.addAttribute("prioridades", Prioridad.values());
    model.addAttribute("persistente", environment.matchesProfiles("mysql"));
    model.addAttribute(
        "demoHabilitada", environment.getProperty("app.demo.enabled", Boolean.class, false));
  }

  @ExceptionHandler({
    org.springframework.dao.OptimisticLockingFailureException.class,
    jakarta.persistence.OptimisticLockException.class,
    org.springframework.dao.DataIntegrityViolationException.class,
    org.hibernate.exception.ConstraintViolationException.class
  })
  public String conflicto(Exception e, Model model, HttpServletResponse response) {
    response.setStatus(409);
    model.addAttribute("status", 409);
    model.addAttribute(
        "mensaje",
        "Los datos cambiaron o entran en conflicto con un registro existente. Recarga la página y"
            + " revisa la operación.");
    return "error";
  }

  @ExceptionHandler(ReglaNegocioException.class)
  public String regla(ReglaNegocioException e, Model model, HttpServletResponse response) {
    response.setStatus(400);
    model.addAttribute("status", 400);
    model.addAttribute("mensaje", e.getMessage());
    return "error";
  }
}
