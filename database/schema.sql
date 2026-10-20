-- Hito 1 APF3. Ejecutar una sola vez sobre una base vacía MySQL 8.4.
-- No borra ni reemplaza tablas existentes. La aplicación todavía usa memoria.
-- Los códigos OS-0001 se derivan del id; no se almacenan por duplicado.
SET NAMES utf8mb4;

CREATE TABLE cliente (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL DEFAULT '',
    documento VARCHAR(20) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_cliente_documento UNIQUE (documento),
    CONSTRAINT ck_cliente_nombres CHECK (CHAR_LENGTH(TRIM(nombres)) > 0),
    CONSTRAINT ck_cliente_documento CHECK (CHAR_LENGTH(TRIM(documento)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE tecnico (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombres VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uq_tecnico_nombres UNIQUE (nombres),
    CONSTRAINT ck_tecnico_nombres CHECK (CHAR_LENGTH(TRIM(nombres)) > 0),
    CONSTRAINT ck_tecnico_activo CHECK (activo IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(120) NOT NULL,
    clave_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    tecnico_id BIGINT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT uq_usuario_tecnico UNIQUE (tecnico_id),
    CONSTRAINT fk_usuario_tecnico FOREIGN KEY (tecnico_id) REFERENCES tecnico (id),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMINISTRADOR', 'ADMINISTRATIVO', 'TECNICO')),
    CONSTRAINT ck_usuario_vinculo CHECK (
        (rol = 'TECNICO' AND tecnico_id IS NOT NULL)
        OR (rol IN ('ADMINISTRADOR', 'ADMINISTRATIVO') AND tecnico_id IS NULL)
    ),
    CONSTRAINT ck_usuario_activo CHECK (activo IN (0, 1)),
    CONSTRAINT ck_usuario_nombre CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),
    CONSTRAINT ck_usuario_hash CHECK (CHAR_LENGTH(TRIM(clave_hash)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE equipo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    marca VARCHAR(60) NOT NULL,
    modelo VARCHAR(80) NOT NULL,
    numero_serie VARCHAR(80) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_equipo_serie UNIQUE (numero_serie),
    CONSTRAINT uq_equipo_propietario UNIQUE (id, cliente_id),
    CONSTRAINT fk_equipo_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT ck_equipo_serie CHECK (CHAR_LENGTH(TRIM(numero_serie)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE orden_servicio (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    equipo_id BIGINT NOT NULL,
    tecnico_id BIGINT NULL,
    fecha_recepcion DATE NOT NULL,
    falla_reportada VARCHAR(1500) NOT NULL,
    prioridad VARCHAR(10) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'REGISTRADA',
    observaciones VARCHAR(1500) NOT NULL DEFAULT '',
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_orden_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_orden_equipo_propietario FOREIGN KEY (equipo_id, cliente_id)
        REFERENCES equipo (id, cliente_id),
    CONSTRAINT fk_orden_tecnico FOREIGN KEY (tecnico_id) REFERENCES tecnico (id),
    CONSTRAINT ck_orden_prioridad CHECK (prioridad IN ('BAJA', 'MEDIA', 'ALTA')),
    CONSTRAINT ck_orden_estado CHECK (
        estado IN ('REGISTRADA', 'ASIGNADA', 'EN_DIAGNOSTICO', 'EN_ATENCION', 'FINALIZADA', 'ENTREGADA')
    ),
    CONSTRAINT ck_orden_asignacion CHECK (
        (estado = 'REGISTRADA' AND tecnico_id IS NULL)
        OR (estado <> 'REGISTRADA' AND tecnico_id IS NOT NULL)
    ),
    CONSTRAINT ck_orden_version CHECK (version >= 0),
    CONSTRAINT ck_orden_falla CHECK (CHAR_LENGTH(TRIM(falla_reportada)) > 0),
    INDEX ix_orden_tecnico_estado_fecha (tecnico_id, estado, fecha_recepcion),
    INDEX ix_orden_estado_fecha (estado, fecha_recepcion),
    INDEX ix_orden_equipo_fecha (equipo_id, fecha_recepcion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

-- El diagnóstico comparte la identidad de su orden: cardinalidad 0..1.
CREATE TABLE diagnostico (
    orden_id BIGINT NOT NULL,
    descripcion VARCHAR(2000) NOT NULL,
    fecha DATE NOT NULL,
    PRIMARY KEY (orden_id),
    CONSTRAINT fk_diagnostico_orden FOREIGN KEY (orden_id) REFERENCES orden_servicio (id),
    CONSTRAINT ck_diagnostico_descripcion CHECK (CHAR_LENGTH(TRIM(descripcion)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

-- numero conserva la identidad local del trabajo dentro de su orden.
CREATE TABLE trabajo_realizado (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orden_id BIGINT NOT NULL,
    numero BIGINT NOT NULL,
    descripcion VARCHAR(2000) NOT NULL,
    fecha DATE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_trabajo_orden_numero UNIQUE (orden_id, numero),
    CONSTRAINT fk_trabajo_orden FOREIGN KEY (orden_id) REFERENCES orden_servicio (id),
    CONSTRAINT ck_trabajo_numero CHECK (numero > 0),
    CONSTRAINT ck_trabajo_descripcion CHECK (CHAR_LENGTH(TRIM(descripcion)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;

CREATE TABLE evento_orden (
    id BIGINT NOT NULL AUTO_INCREMENT,
    orden_id BIGINT NOT NULL,
    numero BIGINT NOT NULL,
    fecha DATETIME(6) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_evento_orden_numero UNIQUE (orden_id, numero),
    CONSTRAINT fk_evento_orden FOREIGN KEY (orden_id) REFERENCES orden_servicio (id),
    CONSTRAINT ck_evento_numero CHECK (numero > 0),
    CONSTRAINT ck_evento_descripcion CHECK (CHAR_LENGTH(TRIM(descripcion)) > 0),
    CONSTRAINT ck_evento_autor CHECK (CHAR_LENGTH(TRIM(autor)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_ci;
