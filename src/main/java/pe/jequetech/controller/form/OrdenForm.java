package pe.jequetech.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import pe.jequetech.model.Prioridad;

public class OrdenForm {

  @NotNull(message = "Selecciona un cliente.")
  @Positive(message = "Selecciona un cliente válido.")
  private Long clienteId;

  public Long getClienteId() {
    return clienteId;
  }

  public void setClienteId(Long value) {
    clienteId = value;
  }

  @NotNull(message = "Selecciona un equipo.")
  @Positive(message = "Selecciona un equipo válido.")
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

  @NotNull(message = "La fecha de recepción es obligatoria.")
  @PastOrPresent(message = "La fecha de recepción no puede ser futura.")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaRecepcion = LocalDate.now();

  public LocalDate getFechaRecepcion() {
    return fechaRecepcion;
  }

  public void setFechaRecepcion(LocalDate value) {
    fechaRecepcion = value;
  }

  @NotBlank(message = "La falla reportada es obligatoria.")
  @Size(max = 1500, message = "La falla reportada no puede superar los 1500 caracteres.")
  private String fallaReportada = "";

  public String getFallaReportada() {
    return fallaReportada;
  }

  public void setFallaReportada(String value) {
    fallaReportada = value;
  }

  @NotNull(message = "Selecciona una prioridad.")
  private Prioridad prioridad = Prioridad.MEDIA;

  public Prioridad getPrioridad() {
    return prioridad;
  }

  public void setPrioridad(Prioridad value) {
    prioridad = value;
  }

  @Size(max = 1500, message = "Las observaciones no pueden superar los 1500 caracteres.")
  private String observaciones = "";

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String value) {
    observaciones = value;
  }

  private String motivo = "";

  public String getMotivo() { return motivo; }

  public void setMotivo(String motivo) { this.motivo = motivo; }

  private long version;

  public long getVersion() {
    return version;
  }

  public void setVersion(long value) {
    version = value;
  }
}