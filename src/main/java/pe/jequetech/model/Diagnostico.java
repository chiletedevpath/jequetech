package pe.jequetech.model;

public record Diagnostico(long id, String descripcion, java.time.LocalDate fecha)
    implements Identificable {}
