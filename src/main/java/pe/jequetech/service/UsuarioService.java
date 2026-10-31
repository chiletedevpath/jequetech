package pe.jequetech.service;

import static pe.jequetech.service.Reglas.*;

import java.util.*;
import org.springframework.stereotype.Service;
import pe.jequetech.controller.form.UsuarioForm;
import pe.jequetech.model.*;
import pe.jequetech.repository.Repositorio;

@Service
@org.springframework.transaction.annotation.Transactional
public class UsuarioService {
  private final Repositorio<Usuario> usuarios;
  private final CatalogoService catalogos;

  public UsuarioService(Repositorio<Usuario> usuarios, CatalogoService catalogos) {
    this.usuarios = usuarios;
    this.catalogos = catalogos;
  }

  public boolean habilitado(Usuario u) {
    return u.activo() && (u.rol() != Rol.TECNICO || catalogos.tecnico(u.tecnicoId()).activo());
  }

  public List<Usuario> todos() {
    return usuarios.todos();
  }

  public Usuario buscar(long id) {
    return usuarios.buscar(id).orElseThrow(Reglas::noExiste);
  }

  public Optional<Usuario> autenticar(String correo, String clave) {
    if (correo == null || clave == null || clave.length() > 128) return Optional.empty();
    return todos().stream()
        .filter(u -> u.correo().equalsIgnoreCase(correo.strip()))
        .filter(Usuario::activo)
        .filter(u -> Claves.coincide(clave, u.claveHash()))
        .filter(u -> u.rol() != Rol.TECNICO || catalogos.tecnico(u.tecnicoId()).activo())
        .findFirst();
  }

  public synchronized Usuario guardar(Usuario actor, Long id, UsuarioForm f) {
    rol(actor, Rol.ADMINISTRADOR);
    Usuario old = id == null ? null : buscar(id);
    exigir(f.getRol() != null, "Selecciona un rol.");
    exigir(
        id == null || id != actor.id() || (f.getRol() == Rol.ADMINISTRADOR && f.getActivo()),
        "Conserva tu rol de administrador para mantener el acceso.");
    String email = correo(f.getCorreo());
    exigir(
        todos().stream()
            .noneMatch(u -> u.correo().equals(email) && !Long.valueOf(u.id()).equals(id)),
        "Ya existe un usuario con ese correo.");
    Long tecnicoId = null;
    if (f.getRol() == Rol.TECNICO) {
      tecnicoId = f.getTecnicoId();
      exigir(tecnicoId != null, "Vincula el usuario con un técnico.");
      exigir(catalogos.tecnico(tecnicoId).activo(), "El técnico debe estar activo.");
      Long tid = tecnicoId;
      exigir(
          todos().stream()
              .noneMatch(u -> tid.equals(u.tecnicoId()) && !Long.valueOf(u.id()).equals(id)),
          "Ese técnico ya tiene un usuario vinculado.");
    }
    String clave = f.getClave() == null ? "" : f.getClave();
    exigir(
        old != null && clave.isEmpty() || clave.length() >= 8 && clave.length() <= 128,
        "La contraseña debe contener entre 8 y 128 caracteres.");
    // En una edición, dejar la contraseña vacía conserva la actual.
    String hash = old != null && clave.isEmpty() ? old.claveHash() : Claves.hash(clave);
    return usuarios.guardar(
        new Usuario(
            id == null ? usuarios.siguienteId() : id,
            texto(f.getNombre(), "Nombre", 100),
            email,
            hash,
            f.getRol(),
            tecnicoId,
            f.getActivo()));
  }

  public void crearAdministradorInicial(String nombre, String correo, String clave) {
    if (!usuarios.todos().isEmpty()) return;
    exigir(clave != null && clave.length() >= 12 && clave.length() <= 128,
        "La contraseña inicial debe contener entre 12 y 128 caracteres.");
    usuarios.guardar(new Usuario(usuarios.siguienteId(), texto(nombre, "Nombre", 100),
        correo(correo), Claves.hash(clave), Rol.ADMINISTRADOR, null, true));
  }
}
