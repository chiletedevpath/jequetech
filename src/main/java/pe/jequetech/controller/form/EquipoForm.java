package pe.jequetech.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EquipoForm {

  @NotNull(message = "Selecciona un cliente.")
  private Long clienteId;

  @NotBlank(message = "El tipo de equipo es obligatorio.")
  @Size(max = 50, message = "El tipo no puede superar los 50 caracteres.")
  private String tipo = "";

  @NotBlank(message = "La marca es obligatoria.")
  @Size(max = 60, message = "La marca no puede superar los 60 caracteres.")
  private String marca = "";

  @NotBlank(message = "El modelo es obligatorio.")
  @Size(max = 80, message = "El modelo no puede superar los 80 caracteres.")
  private String modelo = "";

  @NotBlank(message = "El número de serie es obligatorio.")
  @Size(max = 80, message = "El número de serie no puede superar los 80 caracteres.")
  private String numeroSerie = "";

  public Long getClienteId() {
    return clienteId;
  }

  public void setClienteId(Long value) {
    clienteId = value;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String value) {
    tipo = value;
  }

  public String getMarca() {
    return marca;
  }

  public void setMarca(String value) {
    marca = value;
  }

  public String getModelo() {
    return modelo;
  }

  public void setModelo(String value) {
    modelo = value;
  }

  public String getNumeroSerie() {
    return numeroSerie;
  }

  public void setNumeroSerie(String value) {
    numeroSerie = value;
  }
}