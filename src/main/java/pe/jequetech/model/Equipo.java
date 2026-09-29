package pe.jequetech.model;

public record Equipo(
    long id, long clienteId, String tipo, String marca, String modelo, String numeroSerie)
    implements Identificable {
  public String descripcion() {
    return tipo + " " + marca + " " + modelo;
  }
}
