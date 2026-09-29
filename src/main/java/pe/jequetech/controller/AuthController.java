package pe.jequetech.controller;

import jakarta.servlet.http.*;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.jequetech.service.UsuarioService;

@Controller
public class AuthController {
  private final UsuarioService usuarios;

  public AuthController(UsuarioService usuarios) {
    this.usuarios = usuarios;
  }

  @GetMapping("/login")
  public String login(HttpSession session) {
    return session.getAttribute("usuarioId") == null ? "login" : "redirect:/dashboard";
  }

  @PostMapping("/login")
  public String ingresar(
      @RequestParam String correo,
      @RequestParam String clave,
      HttpServletRequest req,
      Model model) {
    var usuario = usuarios.autenticar(correo, clave);
    if (usuario.isEmpty()) {
      model.addAttribute("error", "Correo o contraseña incorrectos, o técnico inactivo.");
      model.addAttribute("correo", correo);
      return "login";
    }
    req.changeSessionId();
    req.getSession().setAttribute("usuarioId", usuario.get().id());
    req.getSession().setAttribute("csrf", UUID.randomUUID().toString());
    return "redirect:/dashboard";
  }

  @PostMapping("/logout")
  public String salir(HttpSession session) {
    session.invalidate();
    return "redirect:/login?salida";
  }
}
