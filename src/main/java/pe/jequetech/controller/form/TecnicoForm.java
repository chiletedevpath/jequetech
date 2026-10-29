package pe.jequetech.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TecnicoForm {
  @NotBlank(message = "Los nombres son obligatorios.")
  @Size(max = 100, message = "Los nombres no pueden superar los 100 caracteres.")
  private String nombres = "";

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String value) {
    nombres = value;
  }

  @NotBlank(message = "La especialidad es obligatoria.")
  @Size(max = 100, message = "La especialidad no puede superar los 100 caracteres.")
  private String especialidad = "";

  public String getEspecialidad() {
    return especialidad;
  }

  public void setEspecialidad(String value) {
    especialidad = value;
  }

  @NotBlank(message = "El teléfono es obligatorio.")
  @Pattern(regexp = "[+0-9 ()-]{7,20}", message = "Ingresa un teléfono válido de 7 a 20 caracteres.")
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
