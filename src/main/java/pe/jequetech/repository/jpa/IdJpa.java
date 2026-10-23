package pe.jequetech.repository.jpa;

import jakarta.persistence.*;
import pe.jequetech.model.*;

@MappedSuperclass
public abstract class IdJpa {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  public Long getId() {
    return id;
  }
}
