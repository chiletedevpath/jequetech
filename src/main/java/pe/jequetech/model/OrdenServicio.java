package pe.jequetech.model;

import java.time.LocalDate;
import java.util.List;

public record OrdenServicio(
    long id,
    long clienteId,
    long equipoId,
    Long tecnicoId,
    LocalDate fechaRecepcion,
    String fallaReportada,
    Prioridad prioridad,
    EstadoOrden estado,
    String observaciones,
    Diagnostico diagnostico,
    List<TrabajoRealizado> trabajos,
    List<EventoOrden> historial,
    long version)
    implements Identificable {
  public OrdenServicio {
    trabajos = List.copyOf(trabajos);
    historial = List.copyOf(historial);
  }

  public String codigo() {
    return "OS-%04d".formatted(id);
  }
}
