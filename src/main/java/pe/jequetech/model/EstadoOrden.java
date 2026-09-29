package pe.jequetech.model;

public enum EstadoOrden {
  REGISTRADA("Registrada"),
  ASIGNADA("Asignada"),
  EN_DIAGNOSTICO("En diagnóstico"),
  EN_ATENCION("En atención"),
  FINALIZADA("Finalizada"),
  ENTREGADA("Entregada");
  private final String etiqueta;

  EstadoOrden(String etiqueta) {
    this.etiqueta = etiqueta;
  }

  public String getEtiqueta() {
    return etiqueta;
  }

  public EstadoOrden siguiente() {
    return this == ENTREGADA ? null : values()[ordinal() + 1];
  }
}
