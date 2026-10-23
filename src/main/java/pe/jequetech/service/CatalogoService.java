package pe.jequetech.service;

import static pe.jequetech.service.Reglas.*;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.jequetech.controller.form.*;
import pe.jequetech.model.*;
import pe.jequetech.repository.Repositorio;

@Service
@org.springframework.transaction.annotation.Transactional
public class CatalogoService {
  private final Repositorio<Cliente> clientes;
  private final Repositorio<Equipo> equipos;
  private final Repositorio<Tecnico> tecnicos;
  private final Repositorio<OrdenServicio> ordenes;
  private final Repositorio<Usuario> usuarios;

  public CatalogoService(
      Repositorio<Cliente> clientes,
      Repositorio<Equipo> equipos,
      Repositorio<Tecnico> tecnicos,
      Repositorio<OrdenServicio> ordenes,
      Repositorio<Usuario> usuarios) {
    this.clientes = clientes;
    this.equipos = equipos;
    this.tecnicos = tecnicos;
    this.ordenes = ordenes;
    this.usuarios = usuarios;
  }

  public List<Cliente> clientes() {
    return clientes.todos();
  }

  public List<Equipo> equipos() {
    return equipos.todos();
  }

  public List<Tecnico> tecnicos() {
    return tecnicos.todos();
  }

  public boolean tecnicoHabilitado(long id) {
    return tecnico(id).activo()
        && usuarios.todos().stream()
            .anyMatch(
                u ->
                    u.activo() && u.rol() == Rol.TECNICO && Long.valueOf(id).equals(u.tecnicoId()));
  }

  public List<Tecnico> disponibles() {
    return tecnicos().stream().filter(t -> tecnicoHabilitado(t.id())).toList();
  }

  public Cliente cliente(long id) {
    return clientes.buscar(id).orElseThrow(Reglas::noExiste);
  }

  public Equipo equipo(long id) {
    return equipos.buscar(id).orElseThrow(Reglas::noExiste);
  }

  public Tecnico tecnico(long id) {
    return tecnicos.buscar(id).orElseThrow(Reglas::noExiste);
  }

  public synchronized Cliente guardarCliente(Usuario actor, Long id, ClienteForm f) {
    rol(actor, Rol.ADMINISTRATIVO);
    if (id != null) cliente(id);
    String doc = texto(f.getDocumento(), "Documento", 20);
    exigir(
        clientes().stream()
            .noneMatch(
                c -> c.documento().equalsIgnoreCase(doc) && !Long.valueOf(c.id()).equals(id)),
        "Ya existe un cliente con ese documento.");
    return clientes.guardar(
        new Cliente(
            id == null ? clientes.siguienteId() : id,
            texto(f.getNombres(), "Nombres o razón social", 100),
            opcional(f.getApellidos(), 100),
            doc,
            telefono(f.getTelefono()),
            correo(f.getCorreo())));
  }

  public synchronized Equipo guardarEquipo(Usuario actor, Long id, EquipoForm f) {
    rol(actor, Rol.ADMINISTRATIVO);
    exigir(f.getClienteId() != null, "Selecciona un cliente.");
    cliente(f.getClienteId());
    if (id != null) {
      Equipo old = equipo(id);
      exigir(
          old.clienteId() == f.getClienteId()
              || ordenes.todos().stream().noneMatch(o -> o.equipoId() == id),
          "No se puede cambiar el propietario de un equipo con órdenes registradas.");
    }
    String serie = texto(f.getNumeroSerie(), "Número de serie", 80);
    exigir(
        equipos().stream()
            .noneMatch(
                e -> e.numeroSerie().equalsIgnoreCase(serie) && !Long.valueOf(e.id()).equals(id)),
        "Ya existe un equipo con ese número de serie.");
    return equipos.guardar(
        new Equipo(
            id == null ? equipos.siguienteId() : id,
            f.getClienteId(),
            texto(f.getTipo(), "Tipo", 50),
            texto(f.getMarca(), "Marca", 60),
            texto(f.getModelo(), "Modelo", 80),
            serie));
  }

  public synchronized Tecnico guardarTecnico(Usuario actor, Long id, TecnicoForm f) {
    rol(actor, Rol.ADMINISTRADOR);
    if (id != null) tecnico(id);
    String nombre = texto(f.getNombres(), "Nombre del técnico", 100);
    exigir(
        tecnicos().stream()
            .noneMatch(
                t -> t.nombres().equalsIgnoreCase(nombre) && !Long.valueOf(t.id()).equals(id)),
        "Ya existe un técnico con ese nombre.");
    if (!f.getActivo() && id != null)
      exigir(
          ordenes.todos().stream()
              .noneMatch(o -> id.equals(o.tecnicoId()) && o.estado() != EstadoOrden.ENTREGADA),
          "Entrega o reasigna las órdenes pendientes antes de desactivar al técnico.");
    return tecnicos.guardar(
        new Tecnico(
            id == null ? tecnicos.siguienteId() : id,
            nombre,
            texto(f.getEspecialidad(), "Especialidad", 100),
            telefono(f.getTelefono()),
            f.getActivo()));
  }
}
