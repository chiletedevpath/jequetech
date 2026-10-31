package pe.jequetech.repository.jpa;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;
import org.hibernate.annotations.ListIndexBase;
import pe.jequetech.model.*;
import pe.jequetech.service.Reglas;

@Entity
@Table(name = "orden_servicio")
public class OrdenJpa extends IdJpa implements FilaJpa<OrdenServicio> {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cliente_id", nullable = false)
  ClienteJpa cliente;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "equipo_id", nullable = false)
  EquipoJpa equipo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tecnico_id")
  TecnicoJpa tecnico;

  @Column(nullable = false)
  LocalDate fechaRecepcion;

  @Column(nullable = false, length = 1500)
  String fallaReportada;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "varchar(10)")
  Prioridad prioridad;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "varchar(20)")
  EstadoOrden estado;

  @Column(nullable = false, length = 1500)
  String observaciones;

  @Version
  @Column(nullable = false)
  long version;

  @OneToOne(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
  DiagnosticoJpa diagnostico;

  @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("numero")
  List<TrabajoJpa> trabajos = new ArrayList<>();

  // La colección es parte del agregado: añadir un evento también incrementa @Version.
  @ElementCollection
  @CollectionTable(name = "evento_orden", joinColumns = @JoinColumn(name = "orden_id"))
  @OrderColumn(name = "numero", columnDefinition = "bigint")
  @ListIndexBase(1)
  List<EventoJpa> historial = new ArrayList<>();

  public OrdenJpa() {}

  public OrdenServicio dominio() {
    return new OrdenServicio(
        id,
        cliente.getId(),
        equipo.getId(),
        tecnico == null ? null : tecnico.getId(),
        fechaRecepcion,
        fallaReportada,
        prioridad,
        estado,
        observaciones,
        diagnostico == null ? null : diagnostico.dominio(),
        trabajos.stream().map(TrabajoJpa::dominio).toList(),
        historial.stream().map(EventoJpa::dominio).toList(),
        version);
  }

  public void actualizar(OrdenServicio v, EntityManager em) {
    if (id != null) {
      Reglas.exigir(
          v.version() == version + 1,
          "La orden cambió en otra ventana. Recarga la página antes de continuar.");
      Reglas.exigir(
          v.historial().size() > historial.size(),
          "La actualización necesita un evento de seguimiento.");
      Reglas.exigir(
          v.historial().subList(0, historial.size()).equals(
              historial.stream().map(EventoJpa::dominio).toList()),
          "El historial existente no se puede modificar.");
    }
    cliente = em.getReference(ClienteJpa.class, v.clienteId());
    equipo = em.getReference(EquipoJpa.class, v.equipoId());
    tecnico = v.tecnicoId() == null ? null : em.getReference(TecnicoJpa.class, v.tecnicoId());
    fechaRecepcion = v.fechaRecepcion();
    fallaReportada = v.fallaReportada();
    prioridad = v.prioridad();
    estado = v.estado();
    observaciones = v.observaciones();
    if (v.diagnostico() != null) {
      if (diagnostico == null) {
        diagnostico = new DiagnosticoJpa();
        diagnostico.orden = this;
      }
      diagnostico.descripcion = v.diagnostico().descripcion();
      diagnostico.fecha = v.diagnostico().fecha();
    }
    for (TrabajoRealizado t : v.trabajos()) {
      TrabajoJpa fila = trabajos.stream().filter(x -> x.numero == t.id()).findFirst().orElse(null);
      if (fila == null) {
        fila = new TrabajoJpa();
        fila.orden = this;
        fila.numero = t.id();
        trabajos.add(fila);
      }
      fila.descripcion = t.descripcion();
      fila.fecha = t.fecha();
    }
    for (int i = historial.size(); i < v.historial().size(); i++)
      historial.add(new EventoJpa(v.historial().get(i)));
  }
}
