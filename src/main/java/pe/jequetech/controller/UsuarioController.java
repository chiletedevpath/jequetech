package pe.jequetech.controller;

import static pe.jequetech.service.Reglas.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.jequetech.controller.form.UsuarioForm;
import pe.jequetech.model.*;
import pe.jequetech.service.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
  private final UsuarioService servicio;
  private final CatalogoService catalogos;

  public UsuarioController(UsuarioService servicio, CatalogoService catalogos) {
    this.servicio = servicio;
    this.catalogos = catalogos;
  }

  @ModelAttribute
  public void datos(@RequestAttribute Usuario actor, Model model) {
    rol(actor, Rol.ADMINISTRADOR);
    model.addAttribute("roles", Rol.values());
    model.addAttribute("catalogos", catalogos);
  }

  @GetMapping
  public String lista(Model model) {
    model.addAttribute("registros", servicio.todos());
    return "usuarios/lista";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new UsuarioForm());
    return formulario(model, null);
  }

  @GetMapping("/{id}/editar")
  public String editar(@PathVariable long id, Model model) {
    var u = servicio.buscar(id);
    var f = new UsuarioForm();
    f.setActivo(u.activo());
    f.setNombre(u.nombre());
    f.setCorreo(u.correo());
    f.setRol(u.rol());
    f.setTecnicoId(u.tecnicoId());
    model.addAttribute("form", f);
    return formulario(model, id);
  }

  @PostMapping
  public String crear(
      @RequestAttribute Usuario actor,
      @ModelAttribute("form") UsuarioForm f,
      BindingResult binding,
      Model model,
      RedirectAttributes flash) {
    return guardar(actor, null, f, binding, model, flash);
  }

  @PostMapping("/{id}/editar")
  public String actualizar(
      @RequestAttribute Usuario actor,
      @PathVariable long id,
      @ModelAttribute("form") UsuarioForm f,
      BindingResult binding,
      Model model,
      RedirectAttributes flash) {
    return guardar(actor, id, f, binding, model, flash);
  }

  private String guardar(
      Usuario actor,
      Long id,
      UsuarioForm f,
      BindingResult binding,
      Model model,
      RedirectAttributes flash) {
    try {
      exigir(!binding.hasErrors(), "Revisa el rol y el técnico seleccionado.");
      servicio.guardar(actor, id, f);
      flash.addFlashAttribute("exito", "Usuario guardado correctamente.");
      return "redirect:/usuarios";
    } catch (ReglaNegocioException e) {
      f.setClave("");
      model.addAttribute("error", e.getMessage());
      return formulario(model, id);
    }
  }

  private String formulario(Model model, Long id) {
    model.addAttribute("id", id);
    return "usuarios/formulario";
  }
}
