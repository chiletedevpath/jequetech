package pe.jequetech.repository.jpa;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.transaction.annotation.Transactional;
import pe.jequetech.model.Identificable;
import pe.jequetech.repository.Repositorio;
import pe.jequetech.service.Reglas;

/** Convierte entre objetos del dominio y entidades administradas por JPA. */
@Transactional
public class RepositorioJpa<T extends Identificable, E extends FilaJpa<T>>
    implements Repositorio<T> {
  protected final EntityManager em;
  private final Class<E> tipo;
  private final Supplier<E> fabrica;

  public RepositorioJpa(EntityManager em, Class<E> tipo, Supplier<E> fabrica) {
    this.em = em;
    this.tipo = tipo;
    this.fabrica = fabrica;
  }

  /**
   * Cero identifica una inserción; MySQL asigna el id al guardar, nunca
   * MAX(id)+1.
   */
  public long siguienteId() {
    return 0;
  }

  @Transactional(readOnly = true)
  public List<T> todos() {
    return em
        .createQuery("select e from " + tipo.getSimpleName() + " e order by e.id", tipo)
        .getResultList()
        .stream()
        .map(FilaJpa::dominio)
        .toList();
  }

  @Transactional(readOnly = true)
  public Optional<T> buscar(long id) {
    return Optional.ofNullable(em.find(tipo, id)).map(FilaJpa::dominio);
  }

  public T guardar(T valor) {
    E entidad = valor.id() == 0 ? fabrica.get() : em.find(tipo, valor.id());
    if (entidad == null)
      throw Reglas.noExiste();
    entidad.actualizar(valor, em);
    if (valor.id() == 0)
      em.persist(entidad);
    // Aplica la escritura antes de devolver el ID o la versión generados.
    em.flush();
    return entidad.dominio();
  }

  public void eliminar(long id) {
    E entidad = em.find(tipo, id);
    if (entidad == null)
      throw Reglas.noExiste();
    em.remove(entidad);
    em.flush();
  }
}
