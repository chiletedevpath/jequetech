package pe.jequetech.model;

public record TrabajoRealizado(long id, String descripcion, java.time.LocalDate fecha)
    implements Identificable {}
