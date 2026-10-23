package pe.jequetech.repository.jpa;

import jakarta.persistence.*;
import pe.jequetech.model.*;

@Entity
@Table(name = "tecnico")
public class TecnicoJpa extends IdJpa implements FilaJpa<Tecnico> {
  @Column(nullable = false, length = 100)
  String nombres;

  @Column(nullable = false, length = 100)
  String especialidad;

  @Column(nullable = false, length = 20)
  String telefono;

  @Column(nullable = false, columnDefinition = "tinyint")
  boolean activo;

  public TecnicoJpa() {}

  public Tecnico dominio() {
    return new Tecnico(id, nombres, especialidad, telefono, activo);
  }

  public void actualizar(Tecnico v, EntityManager em) {
    nombres = v.nombres();
    especialidad = v.especialidad();
    telefono = v.telefono();
    activo = v.activo();
  }
}
