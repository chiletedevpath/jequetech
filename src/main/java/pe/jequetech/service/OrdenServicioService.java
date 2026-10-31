package pe.jequetech.service;

import static pe.jequetech.service.Reglas.*;

import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.jequetech.controller.form.OrdenForm;
import pe.jequetech.model.*;
import pe.jequetech.repository.OrdenServicioRepository;

@Service
@Transactional
public class OrdenServicioService {
  private final OrdenServicioRepository ordenes;
  private final CatalogoService catalogos;

  public OrdenServicioService(OrdenServicioRepository ordenes, CatalogoService catalogos) {
    this.ordenes = ordenes;
    this.catalogos = catalogos;
  }

  public List<OrdenServicio> listar(Usuario actor, String busqueda, EstadoOrden estado) {
    String q = busqueda == null ? "" : busqueda.strip().toLowerCase(Locale.ROOT);
    return ordenes.todos().stream()
        .filter(o -> actor.rol() != Rol.TECNICO || Objects.equals(actor.tecnicoId(), o.tecnicoId()))
        .filter(o -> estado == null || o.estado() == estado)
        .filter(
            o ->
                (o.codigo()
                        + " "
                        + catalogos.cliente(o.clienteId()).nombreCompleto()
                        + " "
                        + catalogos.equipo(o.equipoId()).descripcion()
                        + " "
                        + catalogos.equipo(o.equipoId()).numeroSerie())
                    .toLowerCase(Locale.ROOT)
                    .contains(q))
        .sorted(
            Comparator.comparing(OrdenServicio::fechaRecepcion)
                .thenComparingLong(OrdenServicio::id)
                .reversed())
        .toList();
  }

  public OrdenServicio buscar(Usuario actor, long id) {
    OrdenServicio o = ordenes.buscar(id).orElseThrow(Reglas::noExiste);
    if (actor.rol() == Rol.TECNICO && !Objects.equals(actor.tecnicoId(), o.tecnicoId()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return o;
  }

  private void validar(OrdenForm f) {
    exigir(f.getClienteId() != null && f.getEquipoId() != null, "Selecciona cliente y equipo.");
    catalogos.cliente(f.getClienteId());
    exigir(
        catalogos.equipo(f.getEquipoId()).clienteId() == f.getClienteId(),
        "El equipo seleccionado no pertenece al cliente.");
    exigir(
        f.getFechaRecepcion() != null && !f.getFechaRecepcion().isAfter(LocalDate.now()),
        "La fecha de recepción es obligatoria y no puede ser futura.");
    exigir(f.getPrioridad() != null, "Selecciona la prioridad.");
    texto(f.getFallaReportada(), "Falla reportada", 1500);
    opcional(f.getObservaciones(), 1500);
    if (f.getTecnicoId() != null)
      exigir(
          catalogos.tecnico(f.getTecnicoId()).activo(), "El técnico seleccionado está inactivo.");
  }

  public synchronized OrdenServicio registrar(Usuario actor, OrdenForm f) {
    rol(actor, Rol.ADMINISTRATIVO);
    validar(f);
    List<EventoOrden> eventos = new ArrayList<>();
    eventos.add(evento(actor, "Orden registrada"));
    exigir(
        f.getTecnicoId() == null, "Registra la orden y luego asigna al técnico desde el detalle.");
    return ordenes.guardar(
        new OrdenServicio(
            ordenes.siguienteId(),
            f.getClienteId(),
            f.getEquipoId(),
            f.getTecnicoId(),
            f.getFechaRecepcion(),
            f.getFallaReportada().strip(),
            f.getPrioridad(),
            EstadoOrden.REGISTRADA,
            opcional(f.getObservaciones(), 1500),
            null,
            List.of(),
            eventos,
            0));
  }

  public synchronized OrdenServicio editar(Usuario actor, long id, OrdenForm f) {
    rol(actor, Rol.ADMINISTRATIVO);
    OrdenServicio o = buscar(actor, id);
    version(o, f.getVersion());
    exigir(
        o.estado() == EstadoOrden.REGISTRADA || o.estado() == EstadoOrden.ASIGNADA,
        "Solo se editan los datos de recepción antes de iniciar el diagnóstico.");
    validar(f);
    // La asignación tiene su propia operación y no se altera mediante el formulario de recepción.
    exigir(
        Objects.equals(f.getTecnicoId(), o.tecnicoId()),
        "Usa la opción Asignar técnico para cambiar al responsable.");
    return ordenes.guardar(
        new OrdenServicio(
            id,
            f.getClienteId(),
            f.getEquipoId(),
            o.tecnicoId(),
            f.getFechaRecepcion(),
            f.getFallaReportada().strip(),
            f.getPrioridad(),
            o.estado(),
            opcional(f.getObservaciones(), 1500),
            o.diagnostico(),
            o.trabajos(),
            historial(o, actor, "Datos de recepción actualizados"),
            o.version() + 1));
  }

  public synchronized void asignar(Usuario actor, long id, Long tecnicoId, long version) {
    rol(actor, Rol.ADMINISTRATIVO);
    OrdenServicio o = buscar(actor, id);
    version(o, version);
    exigir(
        o.estado() != EstadoOrden.ENTREGADA,
        "No se puede reasignar una orden entregada.");
    exigir(tecnicoId != null, "Selecciona un técnico.");
    exigir(
        catalogos.tecnicoHabilitado(tecnicoId),
        "Selecciona un técnico activo con una cuenta habilitada.");
    exigir(!Objects.equals(o.tecnicoId(), tecnicoId), "El técnico ya está asignado.");
    guardarAvance(
        o,
        actor,
        tecnicoId,
        o.estado() == EstadoOrden.REGISTRADA ? EstadoOrden.ASIGNADA : o.estado(),
        o.diagnostico(),
        o.trabajos(),
        o.tecnicoId() == null
            ? "Técnico asignado: " + catalogos.tecnico(tecnicoId).nombres()
            : "Técnico cambiado de " + catalogos.tecnico(o.tecnicoId()).nombres()
                + " a " + catalogos.tecnico(tecnicoId).nombres());
  }

  public synchronized void diagnosticar(Usuario actor, long id, String descripcion, long version) {
    rol(actor, Rol.TECNICO);
    OrdenServicio o = buscar(actor, id);
    version(o, version);
    exigir(
        o.estado() == EstadoOrden.ASIGNADA || o.estado() == EstadoOrden.EN_DIAGNOSTICO,
        "El diagnóstico se registra antes de iniciar los trabajos.");
    Diagnostico d = new Diagnostico(id, texto(descripcion, "Diagnóstico", 2000), LocalDate.now());
    guardarAvance(
        o,
        actor,
        o.tecnicoId(),
        EstadoOrden.EN_DIAGNOSTICO,
        d,
        o.trabajos(),
        o.diagnostico() == null
            ? "Diagnóstico registrado · En diagnóstico"
            : "Diagnóstico actualizado");
  }

  public synchronized void trabajo(Usuario actor, long id, String descripcion, long version) {
    rol(actor, Rol.TECNICO);
    OrdenServicio o = buscar(actor, id);
    version(o, version);
    exigir(
        (o.estado() == EstadoOrden.EN_DIAGNOSTICO || o.estado() == EstadoOrden.EN_ATENCION)
            && o.diagnostico() != null,
        "Registra el diagnóstico e inicia la atención antes de añadir trabajos.");
    List<TrabajoRealizado> trabajos = new ArrayList<>(o.trabajos());
    trabajos.add(
        new TrabajoRealizado(
            trabajos.size() + 1, texto(descripcion, "Trabajo realizado", 2000), LocalDate.now()));
    guardarAvance(
        o,
        actor,
        o.tecnicoId(),
        EstadoOrden.EN_ATENCION,
        o.diagnostico(),
        trabajos,
        "Trabajo realizado registrado · En atención");
  }

  public synchronized void cambiarEstado(Usuario actor, long id, EstadoOrden nuevo, long version) {
    rol(actor, Rol.TECNICO);
    OrdenServicio o = buscar(actor, id);
    version(o, version);
    exigir(
        nuevo != null && o.estado() != EstadoOrden.ENTREGADA && nuevo == o.estado().siguiente(),
        "El estado solicitado no corresponde al siguiente paso del flujo.");
    exigir(o.tecnicoId() != null, "Asigna un técnico antes de iniciar la atención.");
    if (nuevo == EstadoOrden.EN_DIAGNOSTICO)
      exigir(o.diagnostico() != null, "Registra el diagnóstico para continuar.");
    if (nuevo == EstadoOrden.EN_ATENCION)
      exigir(o.diagnostico() != null, "Registra el diagnóstico antes de iniciar la atención.");
    if (nuevo == EstadoOrden.FINALIZADA)
      exigir(!o.trabajos().isEmpty(), "Registra al menos un trabajo antes de finalizar.");
    guardarAvance(
        o,
        actor,
        o.tecnicoId(),
        nuevo,
        o.diagnostico(),
        o.trabajos(),
        "Estado actualizado: " + nuevo.getEtiqueta());
  }

  public List<OrdenServicio> historialEquipo(Usuario actor, long equipoId) {
    rol(actor, Rol.ADMINISTRATIVO);
    catalogos.equipo(equipoId);
    return listar(actor, "", null).stream().filter(o -> o.equipoId() == equipoId).toList();
  }

  public EstadoOrden siguientePermitido(OrdenServicio o) {
    if (o.estado() == EstadoOrden.ASIGNADA && o.diagnostico() == null) return null;
    if (o.estado() == EstadoOrden.EN_DIAGNOSTICO && o.diagnostico() == null) return null;
    if (o.estado() == EstadoOrden.EN_ATENCION && o.trabajos().isEmpty()) return null;
    return o.estado().siguiente();
  }

  private void guardarAvance(
      OrdenServicio o,
      Usuario actor,
      Long tecnicoId,
      EstadoOrden estado,
      Diagnostico diagnostico,
      List<TrabajoRealizado> trabajos,
      String mensaje) {
    ordenes.guardar(
        new OrdenServicio(
            o.id(),
            o.clienteId(),
            o.equipoId(),
            tecnicoId,
            o.fechaRecepcion(),
            o.fallaReportada(),
            o.prioridad(),
            estado,
            o.observaciones(),
            diagnostico,
            trabajos,
            historial(o, actor, mensaje),
            o.version() + 1));
  }

  private void version(OrdenServicio o, long version) {
    exigir(
        o.version() == version,
        "La orden cambió en otra ventana. Recarga la página antes de continuar.");
  }

  private EventoOrden evento(Usuario actor, String mensaje) {
    return new EventoOrden(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS), mensaje, actor.nombre());
  }

  private List<EventoOrden> historial(OrdenServicio o, Usuario actor, String mensaje) {
    List<EventoOrden> h = new ArrayList<>(o.historial());
    h.add(evento(actor, mensaje));
    return h;
  }
}
