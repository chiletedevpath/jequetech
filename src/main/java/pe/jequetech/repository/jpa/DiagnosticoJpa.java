package pe.jequetech.repository.jpa;

import jakarta.persistence.*;
import java.time.LocalDate;
import pe.jequetech.model.Diagnostico;

@Entity
@Table(name = "diagnostico")
public class DiagnosticoJpa {
  @Id
  @Column(name = "orden_id")
  Long id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "orden_id")
  OrdenJpa orden;

  @Column(nullable = false, length = 2000)
  String descripcion;

  @Column(nullable = false)
  LocalDate fecha;

  public DiagnosticoJpa() {}

  Diagnostico dominio() {
    return new Diagnostico(id, descripcion, fecha);
  }
}
