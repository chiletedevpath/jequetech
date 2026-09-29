package pe.jequetech.service;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pe.jequetech.model.*;

public final class Reglas {
  private Reglas() {}

  public static void exigir(boolean condicion, String mensaje) {
    if (!condicion) throw new ReglaNegocioException(mensaje);
  }

  public static String texto(String value, String campo, int max) {
    String s = value == null ? "" : value.strip();
    exigir(
        !s.isBlank() && s.length() <= max, campo + ": completa entre 1 y " + max + " caracteres.");
    return s;
  }

  public static String opcional(String value, int max) {
    String s = value == null ? "" : value.strip();
    exigir(s.length() <= max, "El texto no debe superar " + max + " caracteres.");
    return s;
  }

  public static String correo(String value) {
    String s = texto(value, "Correo", 120).toLowerCase(java.util.Locale.ROOT);
    exigir(s.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"), "Ingresa un correo válido.");
    return s;
  }

  public static String telefono(String value) {
    String s = texto(value, "Teléfono", 20);
    exigir(s.matches("[+0-9 ()-]{7,20}"), "Ingresa un teléfono válido.");
    return s;
  }

  public static void rol(Usuario u, Rol... permitidos) {
    if (u == null || Arrays.stream(permitidos).noneMatch(r -> r == u.rol()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso no permitido");
  }

  public static ResponseStatusException noExiste() {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado");
  }
}
