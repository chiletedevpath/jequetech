# Verificación del proyecto

## APF3 — hito 1: base relacional (20 de octubre de 2026)

Se verificó el esquema con MySQL 8.4.11 real, usando la imagen fijada por digest en `compose.yaml` y contenedores exclusivos de prueba. `scripts/check-database.ps1` terminó con 32 comprobaciones SQL aprobadas, ocho tablas creadas y conservación de un dato confirmado después de reiniciar MySQL. Incluye duplicados, relaciones inválidas, cardinalidad de diagnóstico, números locales de trabajos/eventos, rollback y actualización condicionada por versión.

También se comprobó el arranque de Compose, su estado saludable y el acceso a las ocho tablas con el usuario `jequetech`. Los contenedores y volúmenes temporales se retiraron al terminar.

Estas pruebas acreditan el esquema y el almacenamiento de MySQL. La aplicación web sigue usando memoria: entidades JPA, adaptadores, transacciones y persistencia del flujo web corresponden al hito 2. No se modificó código Java ni interfaz en este hito; las pruebas funcionales del APF2 que siguen son evidencia histórica y no una nueva ejecución.

## APF2 — verificación histórica

Fecha: 28 de septiembre de 2026. Proyecto: JequeTech Solutions S.A.C.

## Resultado

La versión local superó 17 pruebas de integración (0 fallos, 0 errores, 0 omitidas) y la prueba funcional en Microsoft Edge mediante Playwright. No se detectaron fallos en los recorridos comprobados. Esto acredita esos recorridos, no una certificación de producción ni una calificación académica.

## Entorno y reproducción

- Windows, Java 25.0.1; compilación con objetivo Java 17.
- Spring Boot 4.0.8, Maven Wrapper 3.9.12 y Bootstrap 5.3.8 local.
- Desde la raíz: `./mvnw.cmd -B verify`.
- Ejecutar: `java -jar target/jequetech-0.1.0.jar`.
- Con la aplicación iniciada y Playwright disponible, ejecutar `node scripts/check-browser.cjs`. Véase README para configurar `PLAYWRIGHT_MODULE`.

Última compilación verificada: BUILD SUCCESS, 12:56, hora de Lima. Informe de pruebas: `target/surefire-reports/pe.jequetech.AplicacionTest.txt`.

## Cobertura comprobada

Las pruebas de integración cubren acceso por roles, sesión y CSRF, vistas, recursos, validación de formularios, propiedad del equipo, registro sin asignación implícita, transiciones de estado, edición concurrente obsoleta, aislamiento del técnico, historial por equipo, cuentas inactivas y protección de la administración. Incluyen la regresión del formato ISO de fecha en el formulario.

La prueba de navegador completó ocho grupos de comprobaciones:

1. Crear y editar clientes.
2. Crear y editar equipos y consultar historial vacío.
3. Registrar, editar y asignar una orden.
4. Registrar diagnóstico y trabajo; cancelar y confirmar el modal; finalizar y entregar.
5. Consultar historial exacto, búsqueda sin resultados y filtro de estado.
6. Crear y editar técnicos y usuarios; comprobar técnico sin órdenes y revocación por cuenta inactiva.
7. Comprobar las páginas de recuperación 403 y 404.
8. Comprobar vistas de 390, 768, 1366 y 1920 píxeles, menú móvil, formularios y tablas.

Resultado: `exports/evidencias/resultado-navegador.json`, PASS, sin fallos registrados. Se generaron 23 capturas. Se revisaron visualmente ejemplos de escritorio, tableta y móvil, incluidos formulario, detalle, inicio de sesión, menú y modal. No hubo errores JavaScript ni respuestas HTTP de servidor de estado 500 o superior durante el recorrido. El desplazamiento horizontal de las tablas queda dentro de su contenedor.

## Alcance y límites

- Los datos son ficticios y se guardan en memoria; reiniciar restaura la demostración. El recorrido automatizado crea registros de prueba.
- La autenticación por sesión, las restricciones por rol y CSRF sirven a esta demostración local. No sustituyen la integración de Spring Security ni una evaluación de producción.
- MySQL, JPA/Hibernate y la migración de las validaciones corresponden a la siguiente etapa; todavía no se ha validado persistencia real.
- El modelo separa controlador, servicio y repositorio para facilitar esa evolución.
- Los documentos Word originales no se modificaron. La selección de capturas, actualización del informe académico y exposición deben realizarse siguiendo `EVIDENCIAS-APF2.md`.
- La correspondencia con casos de uso y rúbrica está en `ALCANCE-APF2.md`; los puntos de exposición e informe no se consideran aprobados automáticamente por pasar pruebas de software.
- El entorno Java 25 mostró advertencias de instrumentación de Mockito durante las pruebas; las pruebas finalizaron correctamente.

## Entrega

`scripts/package.ps1` genera actualmente `exports/jequetech-fuentes.zip` con código, Maven Wrapper, documentación, scripts y esquema MySQL. Excluye `.env`, datos temporales, dependencias descargadas de trabajo, compilados y documentos académicos originales. El ZIP APF2 anterior se conserva como entrega histórica. El JAR ejecutable se encuentra en `target/jequetech-0.1.0.jar` después de la compilación.
