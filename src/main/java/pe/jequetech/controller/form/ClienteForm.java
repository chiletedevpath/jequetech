package pe.jequetech.controller.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ClienteForm {

  @NotBlank(message = "El nombre o razón social es obligatorio.")
  @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
  private String nombres = "";

  @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres.")
  private String apellidos = "";

  @NotBlank(message = "El documento es obligatorio.")
  @Size(max = 20, message = "El documento no puede superar los 20 caracteres.")
  @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "El documento solo puede contener letras, números y guiones.")
  private String documento = "";

  @NotBlank(message = "El teléfono es obligatorio.")
  @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Ingresa un teléfono válido.")
  private String telefono = "";

  @NotBlank(message = "El correo electrónico es obligatorio.")
  @Email(message = "Ingresa un correo electrónico válido.")
  @Size(max = 120, message = "El correo no puede superar los 120 caracteres.")
  private String correo = "";

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String value) {
    nombres = value;
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String value) {
    apellidos = value;
  }

  public String getDocumento() {
    return documento;
  }

  public void setDocumento(String value) {
    documento = value;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String value) {
    telefono = value;
  }

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String value) {
    correo = value;
  }
}