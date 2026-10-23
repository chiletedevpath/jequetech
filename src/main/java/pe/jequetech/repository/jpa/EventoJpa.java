package pe.jequetech.repository.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import pe.jequetech.model.EventoOrden;

@Embeddable
public class EventoJpa {
  @Column(nullable = false, columnDefinition = "datetime(6)")
  LocalDateTime fecha;

  @Column(nullable = false, length = 255)
  String descripcion;

  @Column(nullable = false, length = 100)
  String autor;

  public EventoJpa() {}

  EventoJpa(EventoOrden e) {
    fecha = e.fecha();
    descripcion = e.descripcion();
    autor = e.autor();
  }

  EventoOrden dominio() {
    return new EventoOrden(fecha, descripcion, autor);
  }
}
