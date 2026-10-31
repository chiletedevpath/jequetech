package pe.jequetech.controller;

import static pe.jequetech.service.Reglas.*;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.jequetech.controller.form.OrdenForm;
import pe.jequetech.model.*;
import pe.jequetech.service.*;

@Controller
@RequestMapping("/ordenes")
public class OrdenServicioController {
  private final OrdenServicioService servicio;
  private final CatalogoService catalogos;

  public OrdenServicioController(OrdenServicioService servicio, CatalogoService catalogos) {
    this.servicio = servicio;
    this.catalogos = catalogos;
  }

  @GetMapping
  public String listar(
      @RequestAttribute Usuario actor,
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) EstadoOrden estado,
      Model model) {
    model.addAttribute("ordenes", servicio.listar(actor, q, estado));
    model.addAttribute("q", q);
    model.addAttribute("filtroEstado", estado);
    model.addAttribute("catalogos", catalogos);
    return "ordenes/lista";
  }

  @GetMapping("/nueva")
  public String nueva(@RequestAttribute Usuario actor, Model model) {
    rol(actor, Rol.ADMINISTRATIVO);
    model.addAttribute("form", new OrdenForm());
    return formulario(model, null);
  }

  @GetMapping("/{id}/editar")
  public String editar(@RequestAttribute Usuario actor, @PathVariable long id, Model model) {
    rol(actor, Rol.ADMINISTRATIVO);
    var o = servicio.buscar(actor, id);
    exigir(
        o.estado() == EstadoOrden.REGISTRADA,
        "La recepción ya no se puede editar.");
    OrdenForm f = new OrdenForm();
    f.setClienteId(o.clienteId());
    f.setEquipoId(o.equipoId());
    f.setTecnicoId(o.tecnicoId());
    f.setFechaRecepcion(o.fechaRecepcion());
    f.setPrioridad(o.prioridad());
    f.setFallaReportada(o.fallaReportada());
    f.setObservaciones(o.observaciones());
    f.setVersion(o.version());
    model.addAttribute("form", f);
    return formulario(model, id);
  }

  @PostMapping
  public String registrar(
      @RequestAttribute Usuario actor,
      @Valid @ModelAttribute("form") OrdenForm form,
      BindingResult binding,
      Model model,
      RedirectAttributes flash) {
    return guardar(actor, null, form, binding, model, flash);
  }

  @PostMapping("/{id}/editar")
  public String actualizar(
      @RequestAttribute Usuario actor,
      @PathVariable long id,
      @Valid @ModelAttribute("form") OrdenForm form,
      BindingResult binding,
      Model model,
      RedirectAttributes flash) {
    return guardar(actor, id, form, binding, model, flash);
  }

  private String guardar(
      Usuario actor,
      Long id,
      OrdenForm f,
      BindingResult binding,
      Model model,
      RedirectAttributes flash) {
    rol(actor, Rol.ADMINISTRATIVO);
    try {
      exigir(!binding.hasErrors(), "Revisa las fechas y los valores seleccionados.");
      var o = id == null ? servicio.registrar(actor, f) : servicio.editar(actor, id, f);
      flash.addFlashAttribute(
          "exito",
          id == null ? "Orden registrada correctamente." : "Orden actualizada correctamente.");
      return "redirect:/ordenes/" + o.id();
    } catch (ReglaNegocioException e) {
      model.addAttribute("error", e.getMessage());
      return formulario(model, id);
    }
  }

  private String formulario(Model model, Long id) {
    model.addAttribute("id", id);
    model.addAttribute("clientes", catalogos.clientes());
    model.addAttribute("equipos", catalogos.equipos());
    model.addAttribute("tecnicos", catalogos.disponibles());
    return "ordenes/formulario";
  }

  @GetMapping("/{id}")
  public String detalle(@RequestAttribute Usuario actor, @PathVariable long id, Model model) {
    var o = servicio.buscar(actor, id);
    model.addAttribute("orden", o);
    model.addAttribute("catalogos", catalogos);
    model.addAttribute("tecnicos", catalogos.disponibles());
    model.addAttribute("siguiente", servicio.siguientePermitido(o));
    return "ordenes/detalle";
  }

  @PostMapping("/{id}/asignar")
  public String asignar(
      @RequestAttribute Usuario actor,
      @PathVariable long id,
      @RequestParam(required = false) Long tecnicoId,
      @RequestParam(defaultValue = "") String motivo,
      @RequestParam long version,
      RedirectAttributes flash) {
    return operacion(id, flash, () -> servicio.asignar(actor, id, tecnicoId, version, motivo));
  }

  @PostMapping("/{id}/diagnostico")
  public String diagnostico(
      @RequestAttribute Usuario actor,
      @PathVariable long id,
      @RequestParam String descripcion,
      @RequestParam long version,
      RedirectAttributes flash) {
    return operacion(id, flash, () -> servicio.diagnosticar(actor, id, descripcion, version));
  }

  @PostMapping("/{id}/trabajos")
  public String trabajo(
      @RequestAttribute Usuario actor,
      @PathVariable long id,
      @RequestParam String descripcion,
      @RequestParam long version,
      RedirectAttributes flash) {
    return operacion(id, flash, () -> servicio.trabajo(actor, id, descripcion, version));
  }

  @PostMapping("/{id}/estado")
  public String estado(
      @RequestAttribute Usuario actor,
      @PathVariable long id,
      @RequestParam EstadoOrden estado,
      @RequestParam(defaultValue = "") String receptor,
      @RequestParam long version,
      RedirectAttributes flash) {
    return operacion(id, flash, () -> servicio.cambiarEstado(actor, id, estado, version, receptor));
  }

  @PostMapping("/{id}/aclaraciones")
  public String aclarar(
      @RequestAttribute Usuario actor,
      @PathVariable long id,
      @RequestParam String descripcion,
      @RequestParam long version,
      RedirectAttributes flash) {
    return operacion(id, flash, () -> servicio.aclarar(actor, id, descripcion, version));
  }

  private String operacion(long id, RedirectAttributes flash, Runnable accion) {
    try {
      accion.run();
      flash.addFlashAttribute("exito", "La operación se registró correctamente.");
    } catch (ReglaNegocioException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/ordenes/" + id;
  }
}
