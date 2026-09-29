package pe.jequetech.controller.form;

public class EquipoForm {
  private Long clienteId;

  public Long getClienteId() {
    return clienteId;
  }

  public void setClienteId(Long value) {
    clienteId = value;
  }

  private String tipo = "";

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String value) {
    tipo = value;
  }

  private String marca = "";

  public String getMarca() {
    return marca;
  }

  public void setMarca(String value) {
    marca = value;
  }

  private String modelo = "";

  public String getModelo() {
    return modelo;
  }

  public void setModelo(String value) {
    modelo = value;
  }

  private String numeroSerie = "";

  public String getNumeroSerie() {
    return numeroSerie;
  }

  public void setNumeroSerie(String value) {
    numeroSerie = value;
  }
}
