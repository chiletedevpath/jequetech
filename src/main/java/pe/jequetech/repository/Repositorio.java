package pe.jequetech.repository;

import java.util.List;
import java.util.Optional;
import pe.jequetech.model.Identificable;

public interface Repositorio<T extends Identificable> {
  long siguienteId();

  List<T> todos();

  Optional<T> buscar(long id);

  T guardar(T entidad);
}
