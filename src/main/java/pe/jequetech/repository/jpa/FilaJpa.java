package pe.jequetech.repository.jpa;

import jakarta.persistence.*;
import pe.jequetech.model.*;

public interface FilaJpa<T extends Identificable> {
  T dominio();

  void actualizar(T valor, EntityManager em);
}
