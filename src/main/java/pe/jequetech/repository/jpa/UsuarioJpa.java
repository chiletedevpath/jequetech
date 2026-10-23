package pe.jequetech.repository.jpa;

import jakarta.persistence.*;
import pe.jequetech.model.*;

@Entity
@Table(name = "usuario")
public class UsuarioJpa extends IdJpa implements FilaJpa<Usuario> {
  @Column(nullable = false, length = 100)
  String nombre;

  @Column(nullable = false, length = 120)
  String correo;

  @Column(nullable = false, length = 255)
  String claveHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
  Rol rol;

  @OneToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "tecnico_id", nullable = true)
  TecnicoJpa tecnico;

  @Column(nullable = false, columnDefinition = "tinyint")
  boolean activo;

  public UsuarioJpa() {}

  public Usuario dominio() {
    return new Usuario(
        id, nombre, correo, claveHash, rol, tecnico == null ? null : tecnico.getId(), activo);
  }

  public void actualizar(Usuario v, EntityManager em) {
    nombre = v.nombre();
    correo = v.correo();
    claveHash = v.claveHash();
    rol = v.rol();
    tecnico = v.tecnicoId() == null ? null : em.getReference(TecnicoJpa.class, v.tecnicoId());
    activo = v.activo();
  }
}
