package pe.jequetech;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import pe.jequetech.model.*;
import pe.jequetech.repository.Repositorio;

@SpringBootTest
@AutoConfigureMockMvc
class CrudValidacionTest {
  @Autowired MockMvc mvc;
  @Autowired Repositorio<Cliente> clientes;
  @Autowired Repositorio<Equipo> equipos;
  @Autowired Repositorio<OrdenServicio> ordenes;

  private MockHttpSession login() throws Exception {
    var s = (MockHttpSession) mvc.perform(get("/login")).andReturn().getRequest().getSession();
    mvc.perform(
            post("/login")
                .session(s)
                .param("_csrf", s.getAttribute("csrf").toString())
                .param("correo", "recepcion@jequetech.test")
                .param("clave", "Demo2026!"))
        .andExpect(redirectedUrl("/dashboard"));
    return s;
  }

  private Cliente cliente() {
    String doc = UUID.randomUUID().toString().replace("-", "").substring(0, 18);
    return clientes.guardar(
        new Cliente(
            clientes.siguienteId(),
            "Prueba",
            "Regresión",
            doc,
            "900000099",
            doc + "@example.test"));
  }

  @Test
  void eliminaClienteYDesapareceDeConsulta() throws Exception {
    var c = cliente();
    var s = login();
    try {
      mvc.perform(
              post("/clientes/{id}/eliminar", c.id())
                  .session(s)
                  .param("_csrf", s.getAttribute("csrf").toString()))
          .andExpect(redirectedUrl("/clientes"))
          .andExpect(flash().attribute("exito", "Cliente eliminado correctamente."));
      assertTrue(clientes.buscar(c.id()).isEmpty());
      mvc.perform(get("/clientes").session(s))
          .andExpect(content().string(not(containsString(c.documento()))));
      mvc.perform(get("/clientes/{id}/editar", c.id()).session(s)).andExpect(status().isNotFound());
      assertEquals(
          404,
          assertThrows(ResponseStatusException.class, () -> clientes.eliminar(c.id()))
              .getStatusCode()
              .value());
    } finally {
      if (clientes.buscar(c.id()).isPresent()) clientes.eliminar(c.id());
    }
  }

  @Test
  void conservaClienteConEquipo() throws Exception {
    var c = cliente();
    var e =
        equipos.guardar(
            new Equipo(equipos.siguienteId(), c.id(), "Laptop", "Marca", "Modelo", c.documento()));
    try {
      var s = login();
      mvc.perform(
              post("/clientes/{id}/eliminar", c.id())
                  .session(s)
                  .param("_csrf", s.getAttribute("csrf").toString()))
          .andExpect(redirectedUrl("/clientes"))
          .andExpect(
              flash()
                  .attribute("error", "No se puede eliminar un cliente con equipos registrados."));
      assertEquals(c, clientes.buscar(c.id()).orElseThrow());
    } finally {
      equipos.eliminar(e.id());
      clientes.eliminar(c.id());
    }
  }

  @Test
  void eliminarExigeCsrf() throws Exception {
    var c = cliente();
    try {
      mvc.perform(post("/clientes/{id}/eliminar", c.id()).session(login()))
          .andExpect(status().isForbidden());
      assertEquals(c, clientes.buscar(c.id()).orElseThrow());
    } finally {
      clientes.eliminar(c.id());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void clienteInvalidoConservaValoresSinPersistir(boolean editar) throws Exception {
    var c = cliente();
    var antes = clientes.todos();
    try {
      var s = login();
      mvc.perform(
              post(editar ? "/clientes/" + c.id() + "/editar" : "/clientes")
                  .session(s)
                  .param("_csrf", s.getAttribute("csrf").toString())
                  .param("nombres", "Valor conservado")
                  .param("documento", "invalido!")
                  .param("telefono", "abc")
                  .param("correo", "sin-arroba"))
          .andExpect(status().isOk())
          .andExpect(view().name("clientes/formulario"))
          .andExpect(model().attributeHasFieldErrors("form", "documento", "telefono", "correo"))
          .andExpect(content().string(containsString("Valor conservado")))
          .andExpect(content().string(containsString("invalid-feedback")));
      assertEquals(antes, clientes.todos());
    } finally {
      clientes.eliminar(c.id());
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void equipoInvalidoNoSePersiste(boolean editar) throws Exception {
    var antes = equipos.todos();
    var s = login();
    mvc.perform(
            post(editar ? "/equipos/1/editar" : "/equipos")
                .session(s)
                .param("_csrf", s.getAttribute("csrf").toString())
                .param("clienteId", "1")
                .param("tipo", "")
                .param("marca", "Conservada")
                .param("modelo", "")
                .param("numeroSerie", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("equipos/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "tipo", "modelo", "numeroSerie"))
        .andExpect(content().string(containsString("Conservada")));
    assertEquals(antes, equipos.todos());
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void ordenInvalidaNoSePersiste(boolean editar) throws Exception {
    var antes = ordenes.todos();
    var s = login();
    mvc.perform(
            post(editar ? "/ordenes/1/editar" : "/ordenes")
                .session(s)
                .param("_csrf", s.getAttribute("csrf").toString())
                .param("clienteId", "0")
                .param("equipoId", "0")
                .param("fechaRecepcion", "2999-01-01")
                .param("fallaReportada", "")
                .param("prioridad", "")
                .param("observaciones", "Conservadas"))
        .andExpect(status().isOk())
        .andExpect(
            model()
                .attributeHasFieldErrors(
                    "form",
                    "clienteId",
                    "equipoId",
                    "fechaRecepcion",
                    "fallaReportada",
                    "prioridad"))
        .andExpect(content().string(containsString("Conservadas")));
    assertEquals(antes, ordenes.todos());
  }
}
