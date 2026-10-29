package pe.jequetech.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
  @GetMapping("/login")
  public String login(Authentication autenticacion) {
    return autenticacion == null ? "login" : "redirect:/dashboard";
  }
}
