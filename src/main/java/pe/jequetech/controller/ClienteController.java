package pe.jequetech.controller;

import static pe.jequetech.service.Reglas.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.jequetech.controller.form.ClienteForm;
import pe.jequetech.model.*;
import pe.jequetech.service.*;

@Controller
@RequestMapping("/clientes")
public class ClienteController {
  private final CatalogoService servicio;

  public ClienteController(CatalogoService servicio) {
    this.servicio = servicio;
  }

  @ModelAttribute
  public void datos(@RequestAttribute Usuario actor, Model model) {
    rol(actor, Rol.ADMINISTRATIVO);
    model.addAttribute("catalogos", servicio);
  }

  @GetMapping
  public String lista(Model model) {
    model.addAttribute("registros", servicio.clientes());
    return "clientes/lista";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new ClienteForm());
    return formulario(model, null);
  }

  @GetMapping("/{id}/editar")
  public String editar(@PathVariable long id, Model model) {
    var registro = servicio.cliente(id);
    var f = new ClienteForm();
    f.setNombres(registro.nombres());
    f.setApellidos(registro.apellidos());
    f.setDocumento(registro.documento());
    f.setTelefono(registro.telefono());
    f.setCorreo(registro.correo());
    model.addAttribute("form", f);
    return formulario(model, id);
  }

  @PostMapping
  public String crear(
      @RequestAttribute Usuario actor,
      @ModelAttribute("form") ClienteForm f,
      BindingResult binding,
      Model model,
      RedirectAttributes flash) {
    return guardar(actor, null, f, binding, model, flash);
  }

  @PostMapping("/{id}/editar")
  public String actualizar(
      @RequestAttribute Usuario actor,
      @PathVariable long id,
      @ModelAttribute("form") ClienteForm f,
      BindingResult binding,
      Model model,
      RedirectAttributes flash) {
    return guardar(actor, id, f, binding, model, flash);
  }

  private String guardar(
      Usuario actor,
      Long id,
      ClienteForm f,
      BindingResult binding,
      Model model,
      RedirectAttributes flash) {
    try {
      exigir(!binding.hasErrors(), "Revisa los valores ingresados.");
      servicio.guardarCliente(actor, id, f);
      flash.addFlashAttribute("exito", "Datos guardados correctamente.");
      return "redirect:/clientes";
    } catch (ReglaNegocioException e) {
      model.addAttribute("error", e.getMessage());
      return formulario(model, id);
    }
  }

  private String formulario(Model model, Long id) {
    model.addAttribute("id", id);
    return "clientes/formulario";
  }
}
