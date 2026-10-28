# Verificación del proyecto

## MySQL como único almacenamiento (28 de octubre de 2026)

Se retiraron los repositorios en memoria y la carga de demostración del código de la aplicación. El perfil predeterminado es `mysql`. Los datos ficticios se cargan exclusivamente desde `src/test` en el MySQL aislado del script de verificación; no se incluyen en el JAR. Una base sin usuarios requiere las variables de administrador inicial descritas en el README. No se borran ni reemplazan registros existentes.

Validación: 32 pruebas MySQL sin fallos, errores ni omisiones; arranque del JAR y conservación del mismo hash de datos tras reiniciar aplicación y MySQL. Ejecutar `.\scripts\check-persistence.ps1` para reproducirlo. No se ejecutó el recorrido de navegador en esta validación.

Los apartados siguientes son históricos: sus referencias a memoria, cuentas demo y comandos anteriores describen esos hitos, no la configuración vigente.

## APF3 — cierre de CRUD y Validator (24 de octubre de 2026)

La omisión de pruebas MySQL en el perfil predeterminado es deliberada: `PersistenciaMySqlTest` exige `JEQUETECH_MYSQL_TEST=true`. El script de persistencia establece esa variable, activa `mysql`, crea una base aislada con credenciales temporales y retira su contenedor al terminar. Ahora también exige informes de MySQL con pruebas ejecutadas y rechaza cualquier fallo, error u omisión. No modifica la base de desarrollo.

Se corrigió una diferencia de contrato: eliminar un ID inexistente en memoria ahora produce `Reglas.noExiste()`, como JPA. Se conservan la eliminación física, la transacción del servicio y la prohibición de eliminar clientes con equipos. Las FK mantienen la relación cliente–equipo–orden; no se añadió borrado de otras entidades.

Las anotaciones Jakarta Validation, `@Valid`, `BindingResult` y `th:errors` ya estaban implementadas correctamente. Se añadieron nueve casos de regresión para eliminación, protección de relaciones, CSRF y formularios inválidos de alta/edición de clientes, equipos y órdenes. Verifican errores por campo, valores conservados y ausencia de escrituras. Una prueba adicional exclusiva de MySQL consulta SQL directamente para acreditar eliminación física y rollback.

Resultados Maven del cierre:

- `memoria`: 32 casos descubiertos, 26 ejecutados, seis omitidos intencionalmente; cero fallos y errores.
- `mysql`: 32 ejecutados, cero fallos, errores y omisiones. Incluye seis pruebas exclusivas de persistencia.
- Ambos recorridos ejecutan `verify`, incluida compilación y empaquetado del JAR, con `BUILD SUCCESS`.
- Navegador: diez grupos aprobados, 26 capturas y cero errores registrados. Se inspeccionó visualmente la captura del error de documento y la conservación de los valores del formulario.
- Reinicio real de aplicación y MySQL: `sameDataAfterRestart=true`; los datos conservaron el mismo hash. El script terminó con `PASS` y retiró su contenedor temporal.
- `git diff --check` sin errores. Cambios preparados para revisión, sin publicación remota.

Desde la raíz del repositorio, con Java y Docker disponibles:

```powershell
# Pruebas generales y compilación con el perfil predeterminado.
.\mvnw.cmd -B verify
# MySQL real aislado, pruebas JPA y conservación de datos tras reinicio.
.\scripts\check-persistence.ps1
# Añadir recorrido web y capturas; requiere Node.js, Playwright y Edge.
.\scripts\check-persistence.ps1 -Browser
git diff --check
```

`PLAYWRIGHT_MODULE` permite indicar la ruta de una instalación existente del módulo. Las evidencias de la última ejecución están en `exports/verificacion-mysql/`: `maven.log`, `resultado.json` y `navegador/resultado-navegador.json`. Las capturas adicionales son `apf3-cliente-eliminado.png`, `apf3-eliminacion-protegida.png` y `apf3-validator-servidor.png`. `exports/` permanece fuera de Git.

### Pendiente del informe y anexos

El PDF APF2 y las dos rúbricas se revisaron como fuentes académicas, sin modificar los originales. El cierre técnico no sustituye el informe ni acredita una nota.

- Completar las once secciones del esquema APF3, incluyendo tecnologías, tablas y PK/FK con `database/schema.sql`, servicios y reglas, integración MVC/Thymeleaf–JPA–MySQL, pruebas, trabajo pendiente y al menos tres conclusiones propias sobre el aprendizaje.
- Actualizar CU02 (página física 23 del PDF APF2): añadir eliminación de cliente sin equipos, rechazo si tiene equipos y resultado cuando ya no existe. Los demás casos deben describir el funcionamiento actual.
- Actualizar el diagrama de secuencia (página física 36): reflejar validación del formulario, transacción y persistencia JPA/MySQL, conservando la redirección al detalle de la orden. No presentar `save` como una interfaz Spring Data que el código no utiliza.
- Conservar el diagrama de clases de dominio como tal (página física 39); explicar por separado las entidades JPA y las ocho tablas. Añadir los anexos obligatorios: especificaciones y diagrama de casos de uso, secuencia, clases, capturas reales y código relevante.
- Incorporar capturas de registro, listado, edición, eliminación, validaciones y evidencia de almacenamiento MySQL. Las maquetas del APF2 no prueban el funcionamiento del APF3.
- Para el trabajo final, revisar adicionalmente Project Charter, DER y exposición (demostración, claridad, cámara y participación). La rúbrica final no sustituye la del APF3 ni exige migrar a `JpaRepository`, una API REST o borrar indiscriminadamente otras entidades.

## APF3 — hito 2: JPA y persistencia web (23 de octubre de 2026)

Se incorporó el perfil `mysql` con JPA/Hibernate, relaciones, identificadores autogenerados, transacciones y bloqueo optimista de órdenes. El perfil predeterminado `memoria` conserva la demostración original. Hibernate valida las tablas y no modifica el esquema al arrancar.

`scripts/check-persistence.ps1 -Browser` completó la verificación sobre MySQL 8.4.11 aislado:

- **22 pruebas aprobadas**, sin fallos ni errores: 17 pruebas funcionales usando JPA y cinco pruebas de rollback, semilla no destructiva, actualización del diagnóstico, identificadores y transacciones concurrentes sobre una misma orden.
- **Navegador PASS**, ocho grupos de comprobaciones en Edge y 23 capturas a 390, 768, 1366 y 1920 píxeles. Se revisaron visualmente el acceso móvil y el dashboard de escritorio.
- **Reinicio real de aplicación y MySQL:** el nuevo proceso Java inició correctamente y respondió desde su propio puerto. El volcado ordenado de todos los registros mantuvo el mismo hash antes y después, incluidos los registros del recorrido web.
- La carga demo es explícita y transaccional. Solo se aplica sobre una base sin registros; no repone datos ni cambia contraseñas al reiniciar una base existente.

Evidencia: `exports/verificacion-mysql/maven.log`, `resultado.json`, `arranque.log`, `reinicio.log` y `navegador/resultado-navegador.json`. El script comprueba el PID dueño del puerto HTTP y consulta de nuevo el puerto aleatorio de MySQL después de reiniciarlo. Retira sus procesos, contenedor y volumen al finalizar.

En este hito, el modo memoria se comprobó por separado con `mvnw.cmd -B verify`; las cinco pruebas exclusivas de MySQL se omitieron intencionalmente en ese modo. Todavía no se había incorporado Spring Validator ni Spring Security. No se migran automáticamente datos desde memoria ni se actualizan las entregas históricas del APF2.

## APF3 — hito 1: base relacional (20 de octubre de 2026)

Se verificó el esquema con MySQL 8.4.11 real, usando la imagen fijada por digest en `compose.yaml` y contenedores exclusivos de prueba. `scripts/check-database.ps1` terminó con 32 comprobaciones SQL aprobadas, ocho tablas creadas y conservación de un dato confirmado después de reiniciar MySQL. Incluye duplicados, relaciones inválidas, cardinalidad de diagnóstico, números locales de trabajos/eventos, rollback y actualización condicionada por versión.

También se comprobó el arranque de Compose, su estado saludable y el acceso a las ocho tablas con el usuario `jequetech`. Los contenedores y volúmenes temporales se retiraron al terminar.

Estas pruebas del hito 1 acreditaron únicamente el esquema y el almacenamiento de MySQL. En ese momento la web seguía usando memoria; la integración posterior está documentada en el hito 2 anterior. No se modificó código Java ni interfaz en el hito 1.

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
