package pe.jequetech.controller;

import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.jequetech.model.*;
import pe.jequetech.service.*;

@Controller
public class DashboardController {
  private final OrdenServicioService ordenes;
  private final CatalogoService catalogos;

  public DashboardController(OrdenServicioService ordenes, CatalogoService catalogos) {
    this.ordenes = ordenes;
    this.catalogos = catalogos;
  }

  @GetMapping("/")
  public String inicio() {
    return "redirect:/dashboard";
  }

  @GetMapping("/dashboard")
  public String dashboard(@RequestAttribute Usuario actor, Model model) {
    var lista = ordenes.listar(actor, "", null);
    Map<EstadoOrden, Long> conteos = new EnumMap<>(EstadoOrden.class);
    for (var e : EstadoOrden.values())
      conteos.put(e, lista.stream().filter(o -> o.estado() == e).count());
    model.addAttribute("total", lista.size());
    model.addAttribute("conteos", conteos);
    model.addAttribute("ordenes", lista.stream().limit(5).toList());
    model.addAttribute("catalogos", catalogos);
    return "dashboard";
  }
}
