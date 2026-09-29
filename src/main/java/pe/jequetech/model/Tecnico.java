package pe.jequetech.model;

public record Tecnico(long id, String nombres, String especialidad, String telefono, boolean activo)
    implements Identificable {}
