package pe.jequetech.repository.jpa;

import jakarta.persistence.EntityManager;
import pe.jequetech.model.OrdenServicio;
import pe.jequetech.repository.OrdenServicioRepository;

/**
 * Vincula las órdenes con su entidad JPA. OrdenJpa aplica el control de versión
 * y protege el historial al actualizar una orden.
 */
public class OrdenServicioRepositoryJpa extends RepositorioJpa<OrdenServicio, OrdenJpa>
    implements OrdenServicioRepository {
  public OrdenServicioRepositoryJpa(EntityManager em) {
    super(em, OrdenJpa.class, OrdenJpa::new);
  }
}
