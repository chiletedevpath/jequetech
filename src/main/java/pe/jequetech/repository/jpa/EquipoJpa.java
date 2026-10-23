package pe.jequetech.repository.jpa;

import jakarta.persistence.*;
import pe.jequetech.model.*;

@Entity
@Table(name = "equipo")
public class EquipoJpa extends IdJpa implements FilaJpa<Equipo> {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cliente_id", nullable = false)
  ClienteJpa cliente;

  @Column(nullable = false, length = 50)
  String tipo;

  @Column(nullable = false, length = 60)
  String marca;

  @Column(nullable = false, length = 80)
  String modelo;

  @Column(nullable = false, length = 80)
  String numeroSerie;

  public EquipoJpa() {}

  public Equipo dominio() {
    return new Equipo(id, cliente.getId(), tipo, marca, modelo, numeroSerie);
  }

  public void actualizar(Equipo v, EntityManager em) {
    cliente = em.getReference(ClienteJpa.class, v.clienteId());
    tipo = v.tipo();
    marca = v.marca();
    modelo = v.modelo();
    numeroSerie = v.numeroSerie();
  }
}
