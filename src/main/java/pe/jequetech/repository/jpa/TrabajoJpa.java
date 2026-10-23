package pe.jequetech.repository.jpa;

import jakarta.persistence.*;
import java.time.LocalDate;
import pe.jequetech.model.TrabajoRealizado;

@Entity
@Table(name = "trabajo_realizado")
public class TrabajoJpa extends IdJpa {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "orden_id", nullable = false)
  OrdenJpa orden;

  @Column(nullable = false)
  long numero;

  @Column(nullable = false, length = 2000)
  String descripcion;

  @Column(nullable = false)
  LocalDate fecha;

  public TrabajoJpa() {}

  TrabajoRealizado dominio() {
    return new TrabajoRealizado(numero, descripcion, fecha);
  }
}
