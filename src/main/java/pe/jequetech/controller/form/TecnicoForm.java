package pe.jequetech.controller.form;

public class TecnicoForm {
  private String nombres = "";

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String value) {
    nombres = value;
  }

  private String especialidad = "";

  public String getEspecialidad() {
    return especialidad;
  }

  public void setEspecialidad(String value) {
    especialidad = value;
  }

  private String telefono = "";

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String value) {
    telefono = value;
  }

  private boolean activo = true;

  public boolean getActivo() {
    return activo;
  }

  public void setActivo(boolean value) {
    activo = value;
  }
}
