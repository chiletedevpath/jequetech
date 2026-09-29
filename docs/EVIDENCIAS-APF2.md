# Evidencias para el informe y la exposición

Las imágenes del apartado de maquetación siguen siendo propuestas de diseño. Para demostrar la implementación, incorporar capturas nuevas del sistema ejecutado y distinguirlas con títulos como «Implementación de…». Este archivo no modifica el Word original.

## Capturas que debe incluir el informe

| N.º | Captura exacta | Cómo obtenerla | Criterio |
| --- | --- | --- | --- |
| 1 | Consola con `Started JequetechApplication` y puerto de Tomcat | Ejecutar `mvnw.cmd spring-boot:run` | Spring Boot/Web |
| 2 | Estructura `controller`, `service`, `model`, `repository`, `templates`, `static` y `pom.xml` | Explorador del IDE | Organización e integración |
| 3 | Inicio de sesión con el logo propio | `/login` | HTML/CSS/Bootstrap |
| 4 | Dashboard con cifras y órdenes recientes | Ingresar como recepción | Model y renderizado dinámico |
| 5 | Listado con búsqueda o filtro aplicado | `/ordenes`, seleccionar un estado y Buscar | Thymeleaf, formularios GET y tablas |
| 6 | Formulario de nueva orden completo antes de enviar | Seleccionar un cliente y su equipo | Bootstrap, filas/columnas y enlace de formulario |
| 7 | Detalle de la orden recién registrada, con confirmación y estado Registrada | Pulsar Registrar orden | POST y flujo front–back |
| 8 | Orden Asignada con el nombre del técnico | Asignar desde el detalle | CU-06 y relación de entidades |
| 9 | Diagnóstico y trabajo registrados por el técnico responsable | Entrar con `tecnico@jequetech.test` | CU-08/CU-09 y datos dinámicos |
| 10 | Modal abierto antes de confirmar Finalizada | Actualizar estado tras registrar un trabajo | Ventanas modales Bootstrap |
| 11 | Orden Entregada con historial de cambios | Confirmar Finalizada y después Entregada | Seguimiento y trazabilidad |
| 12 | Historial de servicios de un equipo | Recepción → Equipos → Historial | CU-14 |
| 13 | Dashboard y formulario en móvil de 390 px | Emulación del navegador | Diseño responsive |
| 14 | Menú móvil abierto y vista de tablet de 768 px | Emulación del navegador | Navegación adaptable |
| 15 | Método POST del controller, método `registrar` del service y `th:each` de la tabla | Archivos Java y `fragments/ordenes.html` | Evidencia de integración efectiva |
| 16 | Resultado `Tests run` y `BUILD SUCCESS` | Ejecutar `mvnw.cmd verify` | Estabilidad y validación técnica |

Las capturas automáticas del navegador se guardan en `exports/evidencias/`, junto con `resultado-navegador.json`. No usar una captura de una ejecución fallida como evidencia de aprobación: consultar `docs/VERIFICACION.md` y el resultado más reciente.

## Recorrido de exposición sugerido

Distribuir el tiempo asignado por el docente aproximadamente en: problemática y alcance (15 %), navegación y roles (15 %), demostración del registro/asignación/atención (40 %), explicación controller–service–repository–Thymeleaf (20 %), límites y siguiente avance (10 %). No se presupone una duración que la rúbrica no indica.

Explicar que la memoria es temporal, mostrar una acción que realmente cambie el backend y comprobarla desde otro rol. Mostrar el modal y al menos una vista móvil. Cerrar indicando qué queda para APF3 y para seguridad final. La coherencia, participación, cámara y uso del tiempo deben demostrarse en la exposición; las pruebas de software no acreditan esos puntos.

## Antes de entregar el Word

Actualizar el índice y la lista de figuras en Word, comprobar la numeración tras insertar capturas y revisar que las imágenes sean legibles. Mantener separados maquetación e implementación. En la extracción de texto aparecen títulos de figura repetidos asociados a cuadros de texto; verificar visualmente el Word antes de concluir si existe duplicación visible. No se efectuó una corrección editorial ni una maquetación del documento original.
