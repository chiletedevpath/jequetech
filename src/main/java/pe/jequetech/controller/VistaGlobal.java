package pe.jequetech.controller;

import jakarta.servlet.http.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.jequetech.model.*;
import pe.jequetech.service.*;

@ControllerAdvice
public class VistaGlobal {
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
  }

  @ExceptionHandler(ReglaNegocioException.class)
  public String regla(ReglaNegocioException e, Model model, HttpServletResponse response) {
    response.setStatus(400);
    model.addAttribute("status", 400);
    model.addAttribute("mensaje", e.getMessage());
    return "error";
  }
}
