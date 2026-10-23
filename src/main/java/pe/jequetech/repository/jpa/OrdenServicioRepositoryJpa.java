package pe.jequetech.repository.jpa;

import jakarta.persistence.EntityManager;
import pe.jequetech.model.OrdenServicio;
import pe.jequetech.repository.OrdenServicioRepository;

public class OrdenServicioRepositoryJpa extends RepositorioJpa<OrdenServicio, OrdenJpa>
    implements OrdenServicioRepository {
  public OrdenServicioRepositoryJpa(EntityManager em) {
    super(em, OrdenJpa.class, OrdenJpa::new);
  }
}
