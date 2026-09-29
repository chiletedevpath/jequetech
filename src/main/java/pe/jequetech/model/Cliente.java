package pe.jequetech.model;

public record Cliente(
    long id, String nombres, String apellidos, String documento, String telefono, String correo)
    implements Identificable {
  public String nombreCompleto() {
    return nombres + (apellidos.isBlank() ? "" : " " + apellidos);
  }
}
