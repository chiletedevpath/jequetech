package pe.jequetech.config;

import org.springframework.context.annotation.*;
import pe.jequetech.model.*;
import pe.jequetech.repository.*;
import pe.jequetech.repository.memory.RepositorioMemoria;

@Configuration
@Profile("!mysql")
public class RepositoriosConfig {
  @Bean
  Repositorio<Cliente> clientes() {
    return new RepositorioMemoria<>();
  }

  @Bean
  Repositorio<Equipo> equipos() {
    return new RepositorioMemoria<>();
  }

  @Bean
  Repositorio<Tecnico> tecnicos() {
    return new RepositorioMemoria<>();
  }

  @Bean
  Repositorio<Usuario> usuarios() {
    return new RepositorioMemoria<>();
  }

  @Bean
  OrdenServicioRepository ordenes() {
    return new pe.jequetech.repository.memory.OrdenServicioRepositoryMemoria();
  }
}
