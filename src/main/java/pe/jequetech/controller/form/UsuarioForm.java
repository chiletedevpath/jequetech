package pe.jequetech.controller.form;

public class UsuarioForm {
  private boolean activo = true;

  public boolean getActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  private String nombre = "";

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String value) {
    nombre = value;
  }

  private String correo = "";

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String value) {
    correo = value;
  }

  private String clave = "";

  public String getClave() {
    return clave;
  }

  public void setClave(String value) {
    clave = value;
  }

  private pe.jequetech.model.Rol rol = pe.jequetech.model.Rol.ADMINISTRATIVO;

  public pe.jequetech.model.Rol getRol() {
    return rol;
  }

  public void setRol(pe.jequetech.model.Rol value) {
    rol = value;
  }

  private Long tecnicoId;

  public Long getTecnicoId() {
    return tecnicoId;
  }

  public void setTecnicoId(Long value) {
    tecnicoId = value;
  }
}
