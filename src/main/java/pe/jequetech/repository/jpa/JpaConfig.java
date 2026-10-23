package pe.jequetech.repository.jpa;

import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.*;
import pe.jequetech.model.*;
import pe.jequetech.repository.*;

@Configuration
@Profile("mysql")
public class JpaConfig {
  @Bean
  Repositorio<Cliente> clientesJpa(EntityManager em) {
    return new RepositorioJpa<>(em, ClienteJpa.class, ClienteJpa::new);
  }

  @Bean
  Repositorio<Equipo> equiposJpa(EntityManager em) {
    return new RepositorioJpa<>(em, EquipoJpa.class, EquipoJpa::new);
  }

  @Bean
  Repositorio<Tecnico> tecnicosJpa(EntityManager em) {
    return new RepositorioJpa<>(em, TecnicoJpa.class, TecnicoJpa::new);
  }

  @Bean
  Repositorio<Usuario> usuariosJpa(EntityManager em) {
    return new RepositorioJpa<>(em, UsuarioJpa.class, UsuarioJpa::new);
  }

  @Bean
  OrdenServicioRepository ordenesJpa(EntityManager em) {
    return new OrdenServicioRepositoryJpa(em);
  }
}
