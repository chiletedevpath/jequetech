package pe.jequetech.repository.jpa;

import jakarta.persistence.*;
import pe.jequetech.model.*;

@Entity
@Table(name = "cliente")
public class ClienteJpa extends IdJpa implements FilaJpa<Cliente> {
  @Column(nullable = false, length = 100)
  String nombres;

  @Column(nullable = false, length = 100)
  String apellidos;

  @Column(nullable = false, length = 20)
  String documento;

  @Column(nullable = false, length = 20)
  String telefono;

  @Column(nullable = false, length = 120)
  String correo;

  public ClienteJpa() {}

  public Cliente dominio() {
    return new Cliente(id, nombres, apellidos, documento, telefono, correo);
  }

  public void actualizar(Cliente v, EntityManager em) {
    nombres = v.nombres();
    apellidos = v.apellidos();
    documento = v.documento();
    telefono = v.telefono();
    correo = v.correo();
  }
}
