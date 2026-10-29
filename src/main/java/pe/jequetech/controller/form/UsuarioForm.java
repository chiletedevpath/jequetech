package pe.jequetech.controller.form;

import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;

public class UsuarioForm {
  public interface Creacion extends Default {}

  private boolean activo = true;

  public boolean getActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  @NotBlank(message = "El nombre es obligatorio.")
  @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
  private String nombre = "";

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String value) {
    nombre = value;
  }

  @NotBlank(message = "El correo es obligatorio.")
  @Email(message = "Ingresa un correo electrónico válido.")
  @Size(max = 120, message = "El correo no puede superar los 120 caracteres.")
  private String correo = "";

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String value) {
    correo = value;
  }

  @NotBlank(groups = Creacion.class, message = "La contraseña es obligatoria al crear un usuario.")
  @Pattern(regexp = "(?s)(|.{8,128})", message = "La contraseña debe contener entre 8 y 128 caracteres.")
  private String clave = "";

  public String getClave() {
    return clave;
  }

  public void setClave(String value) {
    clave = value;
  }

  @NotNull(message = "Selecciona un rol.")
  private pe.jequetech.model.Rol rol = pe.jequetech.model.Rol.ADMINISTRATIVO;

  public pe.jequetech.model.Rol getRol() {
    return rol;
  }

  public void setRol(pe.jequetech.model.Rol value) {
    rol = value;
  }

  @Positive(message = "Selecciona un técnico válido.")
  private Long tecnicoId;

  @AssertTrue(message = "Selecciona el técnico que estará vinculado a esta cuenta.")
  public boolean isVinculoTecnicoValido() {
    return rol != pe.jequetech.model.Rol.TECNICO || tecnicoId != null;
  }

  public Long getTecnicoId() {
    return tecnicoId;
  }

  public void setTecnicoId(Long value) {
    tecnicoId = value;
  }
}
