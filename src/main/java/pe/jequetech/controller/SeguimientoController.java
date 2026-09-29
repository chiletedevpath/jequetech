package pe.jequetech.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.jequetech.model.*;
import pe.jequetech.service.*;

@Controller
public class SeguimientoController {
  private final OrdenServicioService ordenes;
  private final CatalogoService catalogos;

  public SeguimientoController(OrdenServicioService ordenes, CatalogoService catalogos) {
    this.ordenes = ordenes;
    this.catalogos = catalogos;
  }

  @GetMapping("/seguimiento")
  public String seguimiento(
      @RequestAttribute Usuario actor,
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) EstadoOrden estado,
      Model model) {
    Reglas.rol(actor, Rol.ADMINISTRATIVO);
    model.addAttribute("modoSeguimiento", true);
    model.addAttribute("ordenes", ordenes.listar(actor, q, estado));
    model.addAttribute("q", q);
    model.addAttribute("filtroEstado", estado);
    model.addAttribute("catalogos", catalogos);
    return "ordenes/lista";
  }
}
