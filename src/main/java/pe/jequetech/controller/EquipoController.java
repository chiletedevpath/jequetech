package pe.jequetech.controller;

import static pe.jequetech.service.Reglas.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.jequetech.controller.form.EquipoForm;
import pe.jequetech.model.*;
import pe.jequetech.service.*;

@Controller
@RequestMapping("/equipos")
public class EquipoController {
  private final CatalogoService servicio;
  private final OrdenServicioService ordenes;

  public EquipoController(CatalogoService servicio, OrdenServicioService ordenes) {
    this.servicio = servicio;
    this.ordenes = ordenes;
  }

  @ModelAttribute
  public void datos(@RequestAttribute Usuario actor, Model model) {
    rol(actor, Rol.ADMINISTRATIVO);
    model.addAttribute("catalogos", servicio);
  }

  @GetMapping("/{id}/historial")
  public String historial(@RequestAttribute Usuario actor, @PathVariable long id, Model model) {
    model.addAttribute("equipo", servicio.equipo(id));
    model.addAttribute("ordenes", ordenes.historialEquipo(actor, id));
    return "equipos/historial";
  }

  @GetMapping
  public String lista(Model model) {
    model.addAttribute("registros", servicio.equipos());
    return "equipos/lista";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new EquipoForm());
    return formulario(model, null);
  }

  @GetMapping("/{id}/editar")
  public String editar(@PathVariable long id, Model model) {
    var registro = servicio.equipo(id);
    var f = new EquipoForm();
    f.setClienteId(registro.clienteId());
    f.setTipo(registro.tipo());
    f.setMarca(registro.marca());
    f.setModelo(registro.modelo());
    f.setNumeroSerie(registro.numeroSerie());
    model.addAttribute("form", f);
    return formulario(model, id);
  }

  @PostMapping
  public String crear(
      @RequestAttribute Usuario actor,
      @ModelAttribute("form") EquipoForm f,
      BindingResult binding,
      Model model,
      RedirectAttributes flash) {
    return guardar(actor, null, f, binding, model, flash);
  }

  @PostMapping("/{id}/editar")
  public String actualizar(
      @RequestAttribute Usuario actor,
      @PathVariable long id,
      @ModelAttribute("form") EquipoForm f,
      BindingResult binding,
      Model model,
      RedirectAttributes flash) {
    return guardar(actor, id, f, binding, model, flash);
  }

  private String guardar(
      Usuario actor,
      Long id,
      EquipoForm f,
      BindingResult binding,
      Model model,
      RedirectAttributes flash) {
    try {
      exigir(!binding.hasErrors(), "Revisa los valores ingresados.");
      servicio.guardarEquipo(actor, id, f);
      flash.addFlashAttribute("exito", "Datos guardados correctamente.");
      return "redirect:/equipos";
    } catch (ReglaNegocioException e) {
      model.addAttribute("error", e.getMessage());
      return formulario(model, id);
    }
  }

  private String formulario(Model model, Long id) {
    model.addAttribute("id", id);
    return "equipos/formulario";
  }
}
