package pe.jequetech;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import pe.jequetech.model.*;
import pe.jequetech.repository.Repositorio;
import pe.jequetech.service.DatosPrueba;
import pe.jequetech.service.UsuarioService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DatosPrueba.class)
@EnabledIfEnvironmentVariable(named = "JEQUETECH_MYSQL_TEST", matches = "true")
class AdministracionValidacionTest {
  @Autowired MockMvc mvc;
  @Autowired Repositorio<Usuario> cuentas;
  @Autowired Repositorio<Tecnico> tecnicos;
  @Autowired UsuarioService usuarios;

  private MockHttpSession admin() throws Exception {
    var sesion = (MockHttpSession) mvc.perform(get("/login")).andReturn().getRequest().getSession();
    mvc.perform(post("/login").session(sesion).with(csrf())
            .param("correo", "admin@jequetech.test").param("clave", "Demo2026!"))
        .andExpect(redirectedUrl("/dashboard"));
    return sesion;
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void usuarioInvalidoConservaCamposSinEscribir(boolean editar) throws Exception {
    var antes = cuentas.todos();
    mvc.perform(post(editar ? "/usuarios/2/editar" : "/usuarios").session(admin()).with(csrf())
            .param("nombre", "Nombre conservado").param("correo", "sin-arroba")
            .param("clave", "corta").param("rol", "ADMINISTRATIVO").param("activo", "true"))
        .andExpect(status().isOk()).andExpect(view().name("usuarios/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "correo", "clave"))
        .andExpect(content().string(containsString("Nombre conservado")))
        .andExpect(content().string(containsString("invalid-feedback")));
    assertEquals(antes, cuentas.todos());
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void tecnicoInvalidoMuestraErroresSinEscribir(boolean editar) throws Exception {
    var antes = tecnicos.todos();
    mvc.perform(post(editar ? "/tecnicos/1/editar" : "/tecnicos").session(admin()).with(csrf())
            .param("nombres", "Nombre conservado").param("especialidad", " ")
            .param("telefono", "abc").param("activo", "true"))
        .andExpect(status().isOk()).andExpect(view().name("tecnicos/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "especialidad", "telefono"))
        .andExpect(content().string(containsString("Nombre conservado")))
        .andExpect(content().string(containsString("invalid-feedback")));
    assertEquals(antes, tecnicos.todos());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "corta", " "})
  void altaExigeContrasenaValida(String clave) throws Exception {
    var antes = cuentas.todos();
    mvc.perform(post("/usuarios").session(admin()).with(csrf())
            .param("nombre", "Cuenta nueva").param("correo", "nueva@example.test")
            .param("clave", clave).param("rol", "ADMINISTRATIVO"))
        .andExpect(status().isOk()).andExpect(view().name("usuarios/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "clave"));
    assertEquals(antes, cuentas.todos());
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void cuentaTecnicaExigeVinculo(boolean editar) throws Exception {
    var antes = cuentas.todos();
    mvc.perform(post(editar ? "/usuarios/2/editar" : "/usuarios").session(admin()).with(csrf())
            .param("nombre", "Cuenta técnica").param("correo", "vinculo@example.test")
            .param("clave", "ClaveValida2026!").param("rol", "TECNICO"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("form", "vinculoTecnicoValido"))
        .andExpect(content().string(containsString("Selecciona el técnico")));
    assertEquals(antes, cuentas.todos());
  }

  @Test
  void edicionConClaveVaciaConservaElHash() throws Exception {
    var original = cuentas.buscar(2).orElseThrow();
    try {
      mvc.perform(post("/usuarios/2/editar").session(admin()).with(csrf())
              .param("nombre", original.nombre()).param("correo", original.correo())
              .param("clave", "").param("rol", "ADMINISTRATIVO").param("activo", "true"))
          .andExpect(redirectedUrl("/usuarios"));
      assertEquals(original.claveHash(), cuentas.buscar(2).orElseThrow().claveHash());
      assertTrue(usuarios.autenticar(original.correo(), "Demo2026!").isPresent());
    } finally { cuentas.guardar(original); }
  }

  @Test
  void valoresNoConvertiblesSeMuestranComoErrores() throws Exception {
    var antes = cuentas.todos();
    mvc.perform(post("/usuarios").session(admin()).with(csrf())
            .param("nombre", "Cuenta").param("correo", "cuenta@example.test")
            .param("clave", "ClaveValida2026!").param("rol", "INVENTADO")
            .param("tecnicoId", "texto"))
        .andExpect(status().isOk()).andExpect(view().name("usuarios/formulario"))
        .andExpect(model().attributeHasFieldErrors("form", "rol", "tecnicoId"))
        .andExpect(content().string(containsString("Selecciona un rol válido")))
        .andExpect(content().string(not(containsString("Failed to convert"))));
    assertEquals(antes, cuentas.todos());
  }

  @Test
  void respuestaInvalidaNoDevuelveLaContrasena() throws Exception {
    var antes = cuentas.todos();
    var resultado = mvc.perform(post("/usuarios").session(admin()).with(csrf())
            .param("nombre", "").param("correo", "cuenta@example.test")
            .param("clave", "ClaveNoPublicar2026!").param("rol", "ADMINISTRATIVO"))
        .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", "nombre"))
        .andExpect(content().string(not(containsString("ClaveNoPublicar2026!"))))
        .andReturn();
    var form = (pe.jequetech.controller.form.UsuarioForm) resultado.getModelAndView().getModel().get("form");
    assertEquals("", form.getClave());
    assertEquals(antes, cuentas.todos());
  }
}
