package pe.jequetech.config;

import java.time.*;
import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.jequetech.model.*;
import pe.jequetech.repository.Repositorio;
import pe.jequetech.service.UsuarioService;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "app.demo.enabled",
    havingValue = "true")
public class DatosDemo implements CommandLineRunner {
  private final Repositorio<Cliente> clientes;
  private final Repositorio<Equipo> equipos;
  private final Repositorio<Tecnico> tecnicos;
  private final Repositorio<OrdenServicio> ordenes;
  private final UsuarioService usuarios;
  private final List<Long> clientesIds = new ArrayList<>();
  private final List<Long> equiposIds = new ArrayList<>();
  private final List<Long> tecnicosIds = new ArrayList<>();

  public DatosDemo(
      Repositorio<Cliente> c,
      Repositorio<Equipo> e,
      Repositorio<Tecnico> t,
      Repositorio<OrdenServicio> o,
      UsuarioService u) {
    clientes = c;
    equipos = e;
    tecnicos = t;
    ordenes = o;
    usuarios = u;
  }

  @org.springframework.transaction.annotation.Transactional
  public void run(String... args) {
    // Nunca reconstruir ni sobrescribir una base que ya tiene registros.
    if (!usuarios.todos().isEmpty()
        || !clientes.todos().isEmpty()
        || !equipos.todos().isEmpty()
        || !tecnicos.todos().isEmpty()
        || !ordenes.todos().isEmpty()) return;
    clientesIds.add(
        clientes
            .guardar(
                new Cliente(
                    clientes.siguienteId(),
                    "Lucía",
                    "Vega",
                    "DEMO-001",
                    "900000001",
                    "lucia@example.test"))
            .id());
    clientesIds.add(
        clientes
            .guardar(
                new Cliente(
                    clientes.siguienteId(),
                    "Comercial Valle Demo",
                    "",
                    "DEMO-002",
                    "900000002",
                    "valle@example.test"))
            .id());
    clientesIds.add(
        clientes
            .guardar(
                new Cliente(
                    clientes.siguienteId(),
                    "Mateo",
                    "Ríos",
                    "DEMO-003",
                    "900000003",
                    "mateo@example.test"))
            .id());
    clientesIds.add(
        clientes
            .guardar(
                new Cliente(
                    clientes.siguienteId(),
                    "Centro Educativo Demo",
                    "",
                    "DEMO-004",
                    "900000004",
                    "centro@example.test"))
            .id());
    equiposIds.add(
        equipos
            .guardar(
                new Equipo(
                    equipos.siguienteId(),
                    clientesIds.get(0),
                    "Laptop",
                    "Lenovo",
                    "IdeaPad 3",
                    "DEMO-LEN-001"))
            .id());
    equiposIds.add(
        equipos
            .guardar(
                new Equipo(
                    equipos.siguienteId(),
                    clientesIds.get(1),
                    "PC",
                    "HP",
                    "ProDesk",
                    "DEMO-HP-002"))
            .id());
    equiposIds.add(
        equipos
            .guardar(
                new Equipo(
                    equipos.siguienteId(),
                    clientesIds.get(2),
                    "Impresora",
                    "Epson",
                    "L3250",
                    "DEMO-EPS-003"))
            .id());
    equiposIds.add(
        equipos
            .guardar(
                new Equipo(
                    equipos.siguienteId(),
                    clientesIds.get(3),
                    "Router",
                    "TP-Link",
                    "Archer C6",
                    "DEMO-TP-004"))
            .id());
    equiposIds.add(
        equipos
            .guardar(
                new Equipo(
                    equipos.siguienteId(),
                    clientesIds.get(0),
                    "Laptop",
                    "Dell",
                    "Inspiron 15",
                    "DEMO-DEL-005"))
            .id());
    tecnicosIds.add(
        tecnicos
            .guardar(
                new Tecnico(
                    tecnicos.siguienteId(),
                    "Daniel Torres",
                    "Hardware y mantenimiento",
                    "900000011",
                    true))
            .id());
    tecnicosIds.add(
        tecnicos
            .guardar(
                new Tecnico(
                    tecnicos.siguienteId(),
                    "Ana Ruiz",
                    "Software y conectividad",
                    "900000012",
                    true))
            .id());
    usuarios.crearDemo("Administrador demo", "admin@jequetech.test", Rol.ADMINISTRADOR, null);
    usuarios.crearDemo("Recepción demo", "recepcion@jequetech.test", Rol.ADMINISTRATIVO, null);
    usuarios.crearDemo("Daniel Torres", "tecnico@jequetech.test", Rol.TECNICO, tecnicosIds.get(0));
    usuarios.crearDemo("Ana Ruiz", "ana@jequetech.test", Rol.TECNICO, tecnicosIds.get(1));
    crearOrden(
        1, 1, 1, 1L, EstadoOrden.EN_DIAGNOSTICO, "El equipo no enciende.", Prioridad.ALTA, 4);
    crearOrden(
        2,
        2,
        2,
        2L,
        EstadoOrden.EN_ATENCION,
        "El sistema se reinicia al abrir programas.",
        Prioridad.MEDIA,
        3);
    crearOrden(
        3,
        3,
        3,
        1L,
        EstadoOrden.FINALIZADA,
        "Impresión con líneas y colores incompletos.",
        Prioridad.MEDIA,
        2);
    crearOrden(
        4,
        4,
        4,
        null,
        EstadoOrden.REGISTRADA,
        "Conexión inalámbrica intermitente.",
        Prioridad.ALTA,
        1);
    crearOrden(
        5, 1, 5, 1L, EstadoOrden.ASIGNADA, "Sobrecalentamiento durante el uso.", Prioridad.BAJA, 0);
    crearOrden(6, 2, 2, 2L, EstadoOrden.ENTREGADA, "Mantenimiento preventivo.", Prioridad.BAJA, 10);
  }

  private void crearOrden(
      long id,
      long cliente,
      long equipo,
      Long tecnico,
      EstadoOrden estado,
      String falla,
      Prioridad prioridad,
      int dias) {
    LocalDate fecha = LocalDate.now().minusDays(dias);
    List<EventoOrden> h = new ArrayList<>();
    for (int i = 0; i <= estado.ordinal(); i++)
      h.add(
          new EventoOrden(
              fecha.atTime(9, 0).plusMinutes(i * 15),
              "Estado: " + EstadoOrden.values()[i].getEtiqueta(),
              "Demostración"));
    Diagnostico d =
        estado.ordinal() >= EstadoOrden.EN_DIAGNOSTICO.ordinal()
            ? new Diagnostico(
                id,
                "Evaluación de demostración: requiere mantenimiento y pruebas de funcionamiento.",
                fecha)
            : null;
    List<TrabajoRealizado> t =
        estado.ordinal() >= EstadoOrden.EN_ATENCION.ordinal()
            ? List.of(
                new TrabajoRealizado(
                    1, "Mantenimiento realizado y funcionamiento verificado.", fecha))
            : List.of();
    ordenes.guardar(
        new OrdenServicio(
            ordenes.siguienteId(),
            clientesIds.get((int) cliente - 1),
            equiposIds.get((int) equipo - 1),
            tecnico == null ? null : tecnicosIds.get(tecnico.intValue() - 1),
            fecha,
            falla,
            prioridad,
            estado,
            "Registro ficticio para demostración académica.",
            d,
            t,
            h,
            0));
  }
}
