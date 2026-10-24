package pe.jequetech.repository;

import java.util.List;
import java.util.Optional;
import pe.jequetech.model.Identificable;

public interface Repositorio<T extends Identificable> {
  /**
   * En memoria reserva un id; JPA devuelve 0 y guardar retorna el id generado por
   * MySQL.
   */
  long siguienteId();

  List<T> todos();

  Optional<T> buscar(long id);

  T guardar(T entidad);

  void eliminar(long id);
}