package pe.jequetech;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import pe.jequetech.controller.form.*;
import pe.jequetech.model.*;
import pe.jequetech.service.*;

@SpringBootTest
@AutoConfigureMockMvc
class AplicacionTest {
  @Autowired MockMvc mvc;
  @Autowired OrdenServicioService ordenes;
  @Autowired CatalogoService catalogos;
  @Autowired UsuarioService usuarios;

  private Usuario recepcion() {
    return usuarios.buscar(2);
  }

  private Usuario tecnico() {
    return usuarios.buscar(3);
  }

  private Usuario admin() {
    return usuarios.buscar(1);
  }

  private OrdenForm formulario() {
    OrdenForm f = new OrdenForm();
    f.setClienteId(1L);
    f.setEquipoId(1L);
    f.setFechaRecepcion(LocalDate.now());
    f.setFallaReportada("Prueba de recepción");
    return f;
  }

  private MockHttpSession login(String correo) throws Exception {
    var session =
        (MockHttpSession)
            mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();
    mvc.perform(
            post("/login")
                .session(session)
                .param("_csrf", session.getAttribute("csrf").toString())
                .param("correo", correo)
                .param("clave", "Demo2026!"))
        .andExpect(redirectedUrl("/dashboard"));
    return session;
  }

  @Test
  void accesoAnonimoYCsrf() throws Exception {
    mvc.perform(get("/ordenes")).andExpect(redirectedUrl("/login"));
    mvc.perform(post("/login").param("correo", "admin@jequetech.test").param("clave", "Demo2026!"))
        .andExpect(status().isForbidden());
  }

  @Test
  void loginInvalidoYSalida() throws Exception {
    var s = (MockHttpSession) mvc.perform(get("/login")).andReturn().getRequest().getSession();
    mvc.perform(
            post("/login")
                .session(s)
                .param("_csrf", s.getAttribute("csrf").toString())
                .param("correo", "admin@jequetech.test")
                .param("clave", "incorrecta"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("incorrectos")));
    var logged = login("recepcion@jequetech.test");
    mvc.perform(
            post("/logout").session(logged).param("_csrf", logged.getAttribute("csrf").toString()))
        .andExpect(redirectedUrl("/login?salida"));
    assertTrue(logged.isInvalid());
  }

  @Test
  void renderizaPantallasRecepcion() throws Exception {
    var s = login("recepcion@jequetech.test");
    for (String ruta :
        new String[] {
          "/dashboard",
          "/ordenes",
          "/ordenes/nueva",
          "/ordenes/1",
          "/clientes",
          "/clientes/nuevo",
          "/clientes/1/editar",
          "/equipos",
          "/equipos/nuevo",
          "/equipos/1/editar",
          "/equipos/1/historial",
          "/seguimiento"
        }) {
      mvc.perform(get(ruta).session(s))
          .andExpect(status().isOk())
          .andExpect(content().string(not(containsString("th:field"))));
    }
  }

  @Test
  void renderizaAdministradorYTecnico() throws Exception {
    var a = login("admin@jequetech.test");
    for (String ruta :
        new String[] {
          "/usuarios",
          "/usuarios/nuevo",
          "/usuarios/2/editar",
          "/tecnicos",
          "/tecnicos/nuevo",
          "/tecnicos/1/editar"
        }) mvc.perform(get(ruta).session(a)).andExpect(status().isOk());
    var t = login("tecnico@jequetech.test");
    mvc.perform(get("/ordenes/1").session(t)).andExpect(status().isOk());
    mvc.perform(get("/ordenes/2").session(t)).andExpect(status().isForbidden());
    mvc.perform(get("/usuarios").session(t)).andExpect(status().isForbidden());
    mvc.perform(get("/clientes").session(a)).andExpect(status().isForbidden());
  }

  @Test
  void recursosLocalesYNoEncontrado() throws Exception {
    for (String ruta :
        new String[] {
          "/css/app.css",
          "/js/app.js",
          "/vendor/bootstrap.min.css",
          "/vendor/bootstrap.bundle.min.js",
          "/img/jequetech-logo.png",
          "/img/favicon.svg"
        }) mvc.perform(get(ruta)).andExpect(status().isOk());
    mvc.perform(get("/ordenes/999999").session(login("recepcion@jequetech.test")))
        .andExpect(status().isNotFound());
  }

  @Test
  void flujoCompletoHastaEntrega() {
    var o = ordenes.registrar(recepcion(), formulario());
    assertEquals(EstadoOrden.REGISTRADA, o.estado());
    assertNull(o.tecnicoId());
    ordenes.asignar(recepcion(), o.id(), 1L, 0);
    o = ordenes.buscar(tecnico(), o.id());
    assertEquals(EstadoOrden.ASIGNADA, o.estado());
    ordenes.diagnosticar(tecnico(), o.id(), "Falla en alimentación", o.version());
    o = ordenes.buscar(tecnico(), o.id());
    assertEquals(EstadoOrden.EN_DIAGNOSTICO, o.estado());
    ordenes.trabajo(tecnico(), o.id(), "Reparación y pruebas", o.version());
    o = ordenes.buscar(tecnico(), o.id());
    assertEquals(EstadoOrden.EN_ATENCION, o.estado());
    ordenes.trabajo(tecnico(), o.id(), "Limpieza final", o.version());
    o = ordenes.buscar(tecnico(), o.id());
    assertEquals(2, o.trabajos().size());
    ordenes.cambiarEstado(tecnico(), o.id(), EstadoOrden.FINALIZADA, o.version());
    o = ordenes.buscar(tecnico(), o.id());
    ordenes.cambiarEstado(tecnico(), o.id(), EstadoOrden.ENTREGADA, o.version());
    o = ordenes.buscar(tecnico(), o.id());
    assertEquals(EstadoOrden.ENTREGADA, o.estado());
    assertEquals(7, o.historial().size());
  }

  @Test
  void validaPropietarioFechaYCampos() {
    var f = formulario();
    f.setEquipoId(2L);
    assertThrows(ReglaNegocioException.class, () -> ordenes.registrar(recepcion(), f));
    f.setEquipoId(1L);
    f.setFechaRecepcion(LocalDate.now().plusDays(1));
    assertThrows(ReglaNegocioException.class, () -> ordenes.registrar(recepcion(), f));
    f.setFechaRecepcion(LocalDate.now());
    f.setFallaReportada(" ");
    assertThrows(ReglaNegocioException.class, () -> ordenes.registrar(recepcion(), f));
  }

  @Test
  void fechaInicialSeRenderizaEnFormatoIso() throws Exception {
    mvc.perform(get("/ordenes/nueva").session(login("recepcion@jequetech.test")))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .string(
                    matchesPattern(
                        "(?s).*<input(?=[^>]*id=\"fechaRecepcion\")(?=[^>]*value=\""
                            + LocalDate.now()
                            + "\")[^>]*>.*")));
  }

  @Test
  void registroNoAsignaImplicitamente() {
    var f = formulario();
    f.setTecnicoId(1L);
    assertThrows(ReglaNegocioException.class, () -> ordenes.registrar(recepcion(), f));
  }

  @Test
  void transicionesYVersionObsoleta() {
    var o = ordenes.registrar(recepcion(), formulario());
    ordenes.asignar(recepcion(), o.id(), 1L, 0);
    assertThrows(ReglaNegocioException.class, () -> ordenes.asignar(recepcion(), o.id(), 2L, 0));
    assertThrows(
        ReglaNegocioException.class,
        () -> ordenes.cambiarEstado(tecnico(), o.id(), EstadoOrden.ENTREGADA, 1));
    assertThrows(
        ReglaNegocioException.class,
        () -> ordenes.trabajo(tecnico(), o.id(), "Sin diagnóstico", 1));
  }

  @Test
  void tecnicoNoModificaOrdenAjena() {
    var o = ordenes.registrar(recepcion(), formulario());
    ordenes.asignar(recepcion(), o.id(), 2L, 0);
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> ordenes.diagnosticar(tecnico(), o.id(), "No autorizado", 1));
    assertTrue(
        ordenes.listar(tecnico(), "", null).stream()
            .allMatch(x -> Long.valueOf(1).equals(x.tecnicoId())));
  }

  @Test
  void historialUsaIdentificadorExacto() {
    var o = ordenes.registrar(recepcion(), formulario());
    var historial = ordenes.historialEquipo(recepcion(), 1);
    assertTrue(historial.stream().anyMatch(x -> x.id() == o.id()));
    assertTrue(historial.stream().allMatch(x -> x.equipoId() == 1));
  }

  @Test
  void formularioInvalidoConservaDatos() throws Exception {
    var s = login("recepcion@jequetech.test");
    mvc.perform(
            post("/ordenes")
                .session(s)
                .param("_csrf", s.getAttribute("csrf").toString())
                .param("clienteId", "1")
                .param("equipoId", "2")
                .param("fechaRecepcion", LocalDate.now().toString())
                .param("prioridad", "MEDIA")
                .param("fallaReportada", "Texto a conservar"))
        .andExpect(status().isOk())
        .andExpect(view().name("ordenes/formulario"))
        .andExpect(content().string(containsString("no pertenece")))
        .andExpect(content().string(containsString("Texto a conservar")));
  }

  @Test
  void registroHttpIntegrado() throws Exception {
    var s = login("recepcion@jequetech.test");
    var result =
        mvc.perform(
                post("/ordenes")
                    .session(s)
                    .param("_csrf", s.getAttribute("csrf").toString())
                    .param("clienteId", "1")
                    .param("equipoId", "1")
                    .param("fechaRecepcion", LocalDate.now().toString())
                    .param("prioridad", "ALTA")
                    .param("fallaReportada", "Registro desde HTTP"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
    mvc.perform(get(result.getResponse().getRedirectedUrl()).session(s))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Registro desde HTTP")));
  }

  @Test
  void duplicadosYCambioPropietario() {
    var f = new ClienteForm();
    f.setNombres("Cliente prueba");
    f.setDocumento("DEMO-001");
    f.setTelefono("900000001");
    f.setCorreo("cliente@example.test");
    assertThrows(ReglaNegocioException.class, () -> catalogos.guardarCliente(recepcion(), null, f));
    var e = new EquipoForm();
    e.setClienteId(2L);
    e.setTipo("PC");
    e.setMarca("Demo");
    e.setModelo("X");
    e.setNumeroSerie("CAMBIO");
    assertThrows(ReglaNegocioException.class, () -> catalogos.guardarEquipo(recepcion(), 1L, e));
  }

  @Test
  void cuentaInactivaNoAutentica() {
    var f = new UsuarioForm();
    f.setNombre("Cuenta prueba");
    f.setCorreo(UUID.randomUUID() + "@example.test");
    f.setClave("Prueba2026!");
    f.setActivo(false);
    var u = usuarios.guardar(admin(), null, f);
    assertFalse(u.activo());
    assertTrue(usuarios.autenticar(u.correo(), "Prueba2026!").isEmpty());
  }

  @Test
  void mantieneAccesoAdministradorYUnicidadTecnico() {
    var f = new UsuarioForm();
    f.setNombre("Admin");
    f.setCorreo("admin@jequetech.test");
    f.setRol(Rol.ADMINISTRADOR);
    f.setActivo(false);
    assertThrows(ReglaNegocioException.class, () -> usuarios.guardar(admin(), admin().id(), f));
    f.setActivo(true);
    f.setCorreo("otro@example.test");
    f.setRol(Rol.TECNICO);
    f.setTecnicoId(1L);
    f.setClave("Prueba2026!");
    assertThrows(ReglaNegocioException.class, () -> usuarios.guardar(admin(), null, f));
  }
}
