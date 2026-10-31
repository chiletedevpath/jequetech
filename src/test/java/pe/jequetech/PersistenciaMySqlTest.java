package pe.jequetech;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pe.jequetech.service.DatosPrueba;
import pe.jequetech.controller.form.OrdenForm;
import pe.jequetech.model.*;
import pe.jequetech.repository.*;
import pe.jequetech.service.*;

@org.springframework.context.annotation.Import(pe.jequetech.service.DatosPrueba.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "JEQUETECH_MYSQL_TEST", matches = "true")
class PersistenciaMySqlTest {
  @Autowired OrdenServicioService servicio;
  @Autowired OrdenServicioRepository ordenes;
  @Autowired Repositorio<Cliente> clientes;
  @Autowired UsuarioService usuarios;
  @Autowired DatosPrueba demo;
  @Autowired PlatformTransactionManager transactionManager;
  @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
  @Autowired CatalogoService catalogos;

  @Test
  void eliminarClienteEsFisicoYTransaccional() {
    String doc = UUID.randomUUID().toString().replace("-", "").substring(0, 18);
    var c =
        clientes.guardar(
            new Cliente(
                clientes.siguienteId(),
                "Rollback",
                "Cliente",
                doc,
                "900000099",
                doc + "@example.test"));
    try {
      assertThrows(
          IllegalStateException.class,
          () ->
              new TransactionTemplate(transactionManager)
                  .execute(
                      status -> {
                        catalogos.eliminarCliente(usuarios.buscar(2), c.id());
                        assertEquals(
                            0,
                            jdbc.queryForObject(
                                "SELECT COUNT(*) FROM cliente WHERE id = ?",
                                Integer.class,
                                c.id()));
                        throw new IllegalStateException("Revertir eliminación");
                      }));
      assertEquals(c, clientes.buscar(c.id()).orElseThrow());
      assertEquals(
          1,
          jdbc.queryForObject("SELECT COUNT(*) FROM cliente WHERE id = ?", Integer.class, c.id()));
      catalogos.eliminarCliente(usuarios.buscar(2), c.id());
      assertEquals(
          0,
          jdbc.queryForObject("SELECT COUNT(*) FROM cliente WHERE id = ?", Integer.class, c.id()));
    } finally {
      if (clientes.buscar(c.id()).isPresent()) clientes.eliminar(c.id());
    }
  }

  private OrdenServicio nueva() {
    var f = new OrdenForm();
    f.setClienteId(1L);
    f.setEquipoId(1L);
    f.setFechaRecepcion(LocalDate.now());
    f.setFallaReportada("Prueba persistente");
    return servicio.registrar(usuarios.buscar(2), f);
  }

  @Test
  void rollbackIncluyeOrdenYEventos() {
    int antes = ordenes.todos().size();
    long[] id = new long[1];
    assertThrows(
        IllegalStateException.class,
        () ->
            new TransactionTemplate(transactionManager)
                .execute(
                    status -> {
                      id[0] = nueva().id();
                      throw new IllegalStateException("Revertir unidad completa");
                    }));
    assertTrue(ordenes.buscar(id[0]).isEmpty());
    assertEquals(antes, ordenes.todos().size());
  }

  @Test
  void semillaNoSobrescribeRegistrosNiClaves() {
    var original = clientes.buscar(1).orElseThrow();
    try {
      clientes.guardar(
          new Cliente(
              original.id(),
              "Edición conservada",
              original.apellidos(),
              original.documento(),
              original.telefono(),
              original.correo()));
      var cuentas = usuarios.todos();
      int cantidad = ordenes.todos().size();
      demo.run();
      assertEquals("Edición conservada", clientes.buscar(1).orElseThrow().nombres());
      assertEquals(cuentas, usuarios.todos());
      assertEquals(cantidad, ordenes.todos().size());
    } finally {
      clientes.guardar(original);
    }
  }

  @Test
  void editarDiagnosticoIncrementaVersionYNoDuplica() {
    var o = nueva();
    servicio.asignar(usuarios.buscar(2), o.id(), 1L, o.version());
    o = ordenes.buscar(o.id()).orElseThrow();
    servicio.diagnosticar(usuarios.buscar(3), o.id(), "Primera evaluación", o.version());
    o = ordenes.buscar(o.id()).orElseThrow();
    long anterior = o.version();
    servicio.diagnosticar(usuarios.buscar(3), o.id(), "Evaluación corregida", anterior);
    var actual = ordenes.buscar(o.id()).orElseThrow();
    assertEquals(anterior + 1, actual.version());
    assertEquals("Evaluación corregida", actual.diagnostico().descripcion());
    assertEquals(actual.id(), actual.diagnostico().id());
    assertEquals(o.historial().size() + 1, actual.historial().size());
  }

  @Test
  void noSePuedeReescribirUnEventoExistente() {
    var o = nueva();
    var eventos = new ArrayList<>(o.historial());
    eventos.set(0, new EventoOrden(eventos.get(0).fecha(), "Recepción adulterada", "Otro"));
    eventos.add(new EventoOrden(LocalDateTime.now(), "Ocultar cambio", "Otro"));
    var alterada = new OrdenServicio(o.id(), o.clienteId(), o.equipoId(), o.tecnicoId(),
        o.fechaRecepcion(), o.fallaReportada(), o.prioridad(), o.estado(), o.observaciones(),
        o.diagnostico(), o.trabajos(), eventos, o.version() + 1);
    assertThrows(ReglaNegocioException.class, () -> ordenes.guardar(alterada));
    assertEquals(o, ordenes.buscar(o.id()).orElseThrow());
  }

  @Test
  void recepcionLargaConservaTodoElTextoEnHistorial() {
    var f = new OrdenForm();
    f.setClienteId(1L);
    f.setEquipoId(1L);
    f.setFechaRecepcion(LocalDate.now());
    f.setFallaReportada("Falla " + "á".repeat(1400));
    f.setObservaciones("Condición " + "🔧".repeat(600));
    var o = servicio.registrar(usuarios.buscar(2), f);
    String guardado = o.historial().stream()
        .filter(e -> e.descripcion().startsWith("Recepción original ["))
        .map(e -> e.descripcion().substring(e.descripcion().indexOf(": ") + 2))
        .collect(java.util.stream.Collectors.joining());
    assertTrue(guardado.contains(f.getFallaReportada()));
    assertTrue(guardado.contains(f.getObservaciones()));
    assertEquals(o, ordenes.buscar(o.id()).orElseThrow());
  }

  @Test
  void idsAutogeneradosNoReutilizanRegistros() {
    var uno = nueva();
    var dos = nueva();
    assertTrue(uno.id() > 0);
    assertTrue(dos.id() > uno.id());
    assertEquals(uno, ordenes.buscar(uno.id()).orElseThrow());
  }

  @Test
  void dosTransaccionesNoPuedenSobrescribirLaMismaVersion() throws Exception {
    var inicial = nueva();
    long id = inicial.id();
    var barrera = new CyclicBarrier(2);
    var pool = Executors.newFixedThreadPool(2);
    Callable<Boolean> modificar =
        () -> {
          try {
            new TransactionTemplate(transactionManager)
                .execute(
                    status -> {
                      var o = ordenes.buscar(id).orElseThrow();
                      try {
                        barrera.await(15, TimeUnit.SECONDS);
                      } catch (Exception e) {
                        throw new IllegalStateException(e);
                      }
                      var eventos = new ArrayList<>(o.historial());
                      eventos.add(
                          new EventoOrden(LocalDateTime.now(), "Edición concurrente", "Prueba"));
                      ordenes.guardar(
                          new OrdenServicio(
                              o.id(),
                              o.clienteId(),
                              o.equipoId(),
                              o.tecnicoId(),
                              o.fechaRecepcion(),
                              o.fallaReportada(),
                              o.prioridad(),
                              o.estado(),
                              "Editada",
                              o.diagnostico(),
                              o.trabajos(),
                              eventos,
                              o.version() + 1));
                      return null;
                    });
            return true;
          } catch (RuntimeException e) {
            Throwable causa = e;
            while (causa != null
                && !(causa instanceof jakarta.persistence.OptimisticLockException)
                && !(causa instanceof org.springframework.dao.OptimisticLockingFailureException))
              causa = causa.getCause();
            if (causa == null) throw e;
            return false;
          }
        };
    try {
      var a = pool.submit(modificar);
      var b = pool.submit(modificar);
      assertNotEquals(a.get(30, TimeUnit.SECONDS), b.get(30, TimeUnit.SECONDS));
      var actual = ordenes.buscar(id).orElseThrow();
      assertEquals(1, actual.version());
      assertEquals(inicial.historial().size() + 1, actual.historial().size());
    } finally {
      pool.shutdownNow();
    }
  }
}
