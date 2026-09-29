package pe.jequetech.repository.memory;

import java.util.*;
import pe.jequetech.model.Identificable;
import pe.jequetech.repository.Repositorio;

/** Adaptador temporal: los registros se pierden al detener la aplicación. */
public class RepositorioMemoria<T extends Identificable> implements Repositorio<T> {
  private final Map<Long, T> datos = new LinkedHashMap<>();
  private long secuencia;

  public synchronized long siguienteId() {
    return ++secuencia;
  }

  public synchronized List<T> todos() {
    return List.copyOf(datos.values());
  }

  public synchronized Optional<T> buscar(long id) {
    return Optional.ofNullable(datos.get(id));
  }

  public synchronized T guardar(T entidad) {
    datos.put(entidad.id(), entidad);
    secuencia = Math.max(secuencia, entidad.id());
    return entidad;
  }
}
