package pe.jequetech.config;

import java.time.*;
import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.jequetech.model.*;
import pe.jequetech.repository.Repositorio;
import pe.jequetech.service.UsuarioService;

@Component
public class DatosDemo implements CommandLineRunner {
  private final Repositorio<Cliente> clientes;
  private final Repositorio<Equipo> equipos;
  private final Repositorio<Tecnico> tecnicos;
  private final Repositorio<OrdenServicio> ordenes;
  private final UsuarioService usuarios;

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

  public void run(String... args) {
    clientes.guardar(
        new Cliente(1, "Lucía", "Vega", "DEMO-001", "900000001", "lucia@example.test"));
    clientes.guardar(
        new Cliente(2, "Comercial Valle Demo", "", "DEMO-002", "900000002", "valle@example.test"));
    clientes.guardar(
        new Cliente(3, "Mateo", "Ríos", "DEMO-003", "900000003", "mateo@example.test"));
    clientes.guardar(
        new Cliente(
            4, "Centro Educativo Demo", "", "DEMO-004", "900000004", "centro@example.test"));
    equipos.guardar(new Equipo(1, 1, "Laptop", "Lenovo", "IdeaPad 3", "DEMO-LEN-001"));
    equipos.guardar(new Equipo(2, 2, "PC", "HP", "ProDesk", "DEMO-HP-002"));
    equipos.guardar(new Equipo(3, 3, "Impresora", "Epson", "L3250", "DEMO-EPS-003"));
    equipos.guardar(new Equipo(4, 4, "Router", "TP-Link", "Archer C6", "DEMO-TP-004"));
    equipos.guardar(new Equipo(5, 1, "Laptop", "Dell", "Inspiron 15", "DEMO-DEL-005"));
    tecnicos.guardar(
        new Tecnico(1, "Daniel Torres", "Hardware y mantenimiento", "900000011", true));
    tecnicos.guardar(new Tecnico(2, "Ana Ruiz", "Software y conectividad", "900000012", true));
    usuarios.crearDemo("Administrador demo", "admin@jequetech.test", Rol.ADMINISTRADOR, null);
    usuarios.crearDemo("Recepción demo", "recepcion@jequetech.test", Rol.ADMINISTRATIVO, null);
    usuarios.crearDemo("Daniel Torres", "tecnico@jequetech.test", Rol.TECNICO, 1L);
    usuarios.crearDemo("Ana Ruiz", "ana@jequetech.test", Rol.TECNICO, 2L);
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
        estado.ordinal() >= EstadoOrden.FINALIZADA.ordinal()
            ? List.of(
                new TrabajoRealizado(
                    1, "Mantenimiento realizado y funcionamiento verificado.", fecha))
            : List.of();
    ordenes.guardar(
        new OrdenServicio(
            id,
            cliente,
            equipo,
            tecnico,
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
