package pe.jequetech.model;

public record Usuario(
    long id,
    String nombre,
    String correo,
    String claveHash,
    Rol rol,
    Long tecnicoId,
    boolean activo)
    implements Identificable {}
