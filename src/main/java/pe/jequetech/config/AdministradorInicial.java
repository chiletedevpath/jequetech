package pe.jequetech.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import pe.jequetech.service.UsuarioService;

/**
 * Crea el primer administrador en una base vacía. Las credenciales de arranque
 * dejan de ser necesarias cuando la base ya contiene usuarios.
 */
@Component
public class AdministradorInicial implements CommandLineRunner {
  private final UsuarioService usuarios;
  private final Environment entorno;

  public AdministradorInicial(UsuarioService usuarios, Environment entorno) {
    this.usuarios = usuarios;
    this.entorno = entorno;
  }

  @Override
  public void run(String... args) {
    // La configuración inicial no modifica cuentas existentes.
    if (!usuarios.todos().isEmpty()) return;
    String correo = entorno.getProperty("JEQUETECH_ADMIN_EMAIL");
    if (correo == null || correo.isBlank()) {
      throw new IllegalStateException(
          "Base sin usuarios: configura JEQUETECH_ADMIN_NAME, JEQUETECH_ADMIN_EMAIL y JEQUETECH_ADMIN_PASSWORD.");
    }
    usuarios.crearAdministradorInicial(
        entorno.getProperty("JEQUETECH_ADMIN_NAME"),
        correo,
        entorno.getProperty("JEQUETECH_ADMIN_PASSWORD"));
  }
}
