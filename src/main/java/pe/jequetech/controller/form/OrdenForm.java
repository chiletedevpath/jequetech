package pe.jequetech.controller.form;

public class OrdenForm {
  private Long clienteId;

  public Long getClienteId() {
    return clienteId;
  }

  public void setClienteId(Long value) {
    clienteId = value;
  }

  private Long equipoId;

  public Long getEquipoId() {
    return equipoId;
  }

  public void setEquipoId(Long value) {
    equipoId = value;
  }

  private Long tecnicoId;

  public Long getTecnicoId() {
    return tecnicoId;
  }

  public void setTecnicoId(Long value) {
    tecnicoId = value;
  }

  @org.springframework.format.annotation.DateTimeFormat(
      iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
  private java.time.LocalDate fechaRecepcion = java.time.LocalDate.now();

  public java.time.LocalDate getFechaRecepcion() {
    return fechaRecepcion;
  }

  public void setFechaRecepcion(java.time.LocalDate value) {
    fechaRecepcion = value;
  }

  private String fallaReportada = "";

  public String getFallaReportada() {
    return fallaReportada;
  }

  public void setFallaReportada(String value) {
    fallaReportada = value;
  }

  private pe.jequetech.model.Prioridad prioridad = pe.jequetech.model.Prioridad.MEDIA;

  public pe.jequetech.model.Prioridad getPrioridad() {
    return prioridad;
  }

  public void setPrioridad(pe.jequetech.model.Prioridad value) {
    prioridad = value;
  }

  private String observaciones = "";

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String value) {
    observaciones = value;
  }

  private long version;

  public long getVersion() {
    return version;
  }

  public void setVersion(long value) {
    version = value;
  }
}
