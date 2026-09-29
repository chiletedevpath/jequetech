package pe.jequetech.model;

public enum Rol {
  ADMINISTRADOR("Administrador"),
  ADMINISTRATIVO("Personal administrativo"),
  TECNICO("Técnico");
  private final String etiqueta;

  Rol(String etiqueta) {
    this.etiqueta = etiqueta;
  }

  public String getEtiqueta() {
    return etiqueta;
  }
}
