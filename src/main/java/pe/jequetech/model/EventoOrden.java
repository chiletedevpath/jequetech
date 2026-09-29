package pe.jequetech.model;

import java.time.LocalDateTime;

public record EventoOrden(LocalDateTime fecha, String descripcion, String autor) {}
