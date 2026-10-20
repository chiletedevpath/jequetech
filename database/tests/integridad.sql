-- Solo lo ejecuta check-database.ps1 en una base efímera independiente.
SET NAMES utf8mb4;
DELIMITER //
CREATE PROCEDURE esperar_error(IN sentencia TEXT, IN esperado INT)
BEGIN
    DECLARE obtenido INT DEFAULT 0;
    DECLARE detalle VARCHAR(128);
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
            GET DIAGNOSTICS CONDITION 1 obtenido = MYSQL_ERRNO;
        SET @sentencia = sentencia;
        PREPARE consulta FROM @sentencia;
        EXECUTE consulta;
        DEALLOCATE PREPARE consulta;
    END;
    IF obtenido <> esperado THEN
        SET detalle = CONCAT('Esperado ', esperado, ', obtenido ', obtenido, ': ', LEFT(sentencia, 80));
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = detalle;
    END IF;
    SET @comprobaciones = @comprobaciones + 1;
END//
CREATE PROCEDURE afirmar(IN condicion BOOLEAN)
BEGIN
    IF condicion IS NULL OR NOT condicion THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se cumplió una comprobación positiva';
    END IF;
    SET @comprobaciones = @comprobaciones + 1;
END//
DELIMITER ;

SET @comprobaciones = 0;
START TRANSACTION;
INSERT INTO cliente (id, nombres, documento, telefono, correo) VALUES
    (1, 'Cliente de prueba', 'TEST-001', '900000001', 'cliente@example.test'),
    (2, 'Otro cliente', 'TEST-002', '900000002', 'otro@example.test');
INSERT INTO tecnico (id, nombres, especialidad, telefono)
    VALUES (1, 'Técnico de prueba', 'Hardware', '900000003');
INSERT INTO usuario (id, nombre, correo, clave_hash, rol, tecnico_id) VALUES
    (1, 'Técnico', 'tecnico@example.test', 'hash-ficticio-no-utilizable', 'TECNICO', 1),
    (2, 'Administrador', 'admin@example.test', 'hash-ficticio-no-utilizable', 'ADMINISTRADOR', NULL),
    (3, 'Recepción', 'recepcion@example.test', 'hash-ficticio-no-utilizable', 'ADMINISTRATIVO', NULL);
INSERT INTO equipo (id, cliente_id, tipo, marca, modelo, numero_serie)
    VALUES (1, 1, 'Laptop', 'Marca', 'Modelo', 'SERIE-001');
INSERT INTO orden_servicio (id, cliente_id, equipo_id, fecha_recepcion, falla_reportada, prioridad)
    VALUES (1, 1, 1, CURRENT_DATE, 'No enciende', 'ALTA'),
           (2, 1, 1, CURRENT_DATE, 'Mantenimiento', 'BAJA');

CALL afirmar((SELECT COUNT(*) = 2 FROM orden_servicio WHERE equipo_id = 1 AND estado = 'REGISTRADA' AND tecnico_id IS NULL));
CALL afirmar((SELECT COUNT(*) = 2 FROM usuario WHERE tecnico_id IS NULL));
CALL esperar_error('UPDATE cliente SET documento = ''test-001'' WHERE id = 2', 1062);
CALL esperar_error('UPDATE usuario SET correo = ''TECNICO@example.test'' WHERE id = 2', 1062);
CALL esperar_error('INSERT INTO tecnico (nombres, especialidad, telefono) VALUES (''Técnico de prueba'', ''Software'', ''900000004'')', 1062);
CALL esperar_error('INSERT INTO equipo (cliente_id, tipo, marca, modelo, numero_serie) VALUES (1, ''PC'', ''Marca'', ''Modelo'', ''serie-001'')', 1062);
CALL esperar_error('INSERT INTO equipo (cliente_id, tipo, marca, modelo, numero_serie) VALUES (999, ''PC'', ''Marca'', ''Modelo'', ''HUERFANO'')', 1452);
CALL esperar_error('UPDATE orden_servicio SET cliente_id = 2 WHERE id = 1', 1452);
CALL esperar_error('UPDATE equipo SET cliente_id = 2 WHERE id = 1', 1451);
CALL esperar_error('UPDATE usuario SET rol = ''TECNICO'', tecnico_id = 1 WHERE id = 2', 1062);
CALL esperar_error('UPDATE usuario SET tecnico_id = NULL WHERE id = 1', 3819);
CALL esperar_error('UPDATE usuario SET rol = ''OTRO'' WHERE id = 2', 3819);
CALL esperar_error('UPDATE usuario SET activo = 2 WHERE id = 2', 3819);
CALL esperar_error('UPDATE orden_servicio SET estado = ''ASIGNADA'' WHERE id = 1', 3819);
CALL esperar_error('UPDATE orden_servicio SET prioridad = ''URGENTE'' WHERE id = 1', 3819);
CALL esperar_error('UPDATE orden_servicio SET version = -1 WHERE id = 1', 3819);
CALL esperar_error('UPDATE orden_servicio SET estado = ''ASIGNADA'', tecnico_id = 999 WHERE id = 1', 1452);
CALL esperar_error('DELETE FROM cliente WHERE id = 1', 1451);

UPDATE orden_servicio SET estado = 'ASIGNADA', tecnico_id = 1, version = version + 1
    WHERE id = 1 AND version = 0;
CALL afirmar(ROW_COUNT() = 1);
UPDATE orden_servicio SET observaciones = 'Actualización obsoleta', version = version + 1
    WHERE id = 1 AND version = 0;
CALL afirmar(ROW_COUNT() = 0);
INSERT INTO diagnostico (orden_id, descripcion, fecha)
    VALUES (1, 'Revisión de alimentación', CURRENT_DATE);
CALL esperar_error('INSERT INTO diagnostico (orden_id, descripcion, fecha) VALUES (1, ''Duplicado'', CURRENT_DATE)', 1062);
CALL esperar_error('INSERT INTO diagnostico (orden_id, descripcion, fecha) VALUES (999, ''Huérfano'', CURRENT_DATE)', 1452);
UPDATE orden_servicio SET estado = 'EN_DIAGNOSTICO', version = version + 1 WHERE id = 1;
INSERT INTO trabajo_realizado (orden_id, numero, descripcion, fecha) VALUES
    (1, 1, 'Limpieza', CURRENT_DATE), (1, 2, 'Pruebas de encendido', CURRENT_DATE);
CALL esperar_error('INSERT INTO trabajo_realizado (orden_id, numero, descripcion, fecha) VALUES (1, 1, ''Duplicado'', CURRENT_DATE)', 1062);
CALL esperar_error('INSERT INTO trabajo_realizado (orden_id, numero, descripcion, fecha) VALUES (999, 1, ''Huérfano'', CURRENT_DATE)', 1452);
INSERT INTO evento_orden (orden_id, numero, fecha, descripcion, autor) VALUES
    (1, 1, CURRENT_TIMESTAMP(6), 'Orden registrada', 'Recepción'),
    (1, 2, CURRENT_TIMESTAMP(6), 'Diagnóstico registrado', 'Técnico');
CALL esperar_error('INSERT INTO evento_orden (orden_id, numero, fecha, descripcion, autor) VALUES (1, 1, NOW(), ''Duplicado'', ''Prueba'')', 1062);
CALL esperar_error('INSERT INTO evento_orden (orden_id, numero, fecha, descripcion, autor) VALUES (999, 1, NOW(), ''Huérfano'', ''Prueba'')', 1452);
CALL esperar_error('DELETE FROM orden_servicio WHERE id = 1', 1451);
CALL afirmar((SELECT COUNT(*) = 2 FROM trabajo_realizado WHERE orden_id = 1));
CALL afirmar((SELECT COUNT(*) = 2 FROM evento_orden WHERE orden_id = 1));
CALL afirmar((SELECT COUNT(*) = 1 FROM diagnostico WHERE orden_id = 1));
ROLLBACK;
CALL afirmar((SELECT COUNT(*) = 0 FROM cliente));
CALL afirmar((SELECT COUNT(*) = 0 FROM orden_servicio));
SELECT CONCAT('PASS: ', @comprobaciones, ' comprobaciones SQL') AS resultado;
DROP PROCEDURE esperar_error;
DROP PROCEDURE afirmar;
