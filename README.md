# JequeTech Solutions S.A.C.

JequeTech es un sistema web para organizar las órdenes de un servicio técnico. Permite saber qué equipo ingresó, a quién pertenece, qué falla presenta, quién lo atiende y qué trabajos se realizaron hasta su entrega.

Es un proyecto académico del curso **Marcos de Desarrollo Web**, desarrollado por **Adrian Pisco**. El flujo principal está implementado y validado en un entorno local.

## Qué hemos avanzado

La primera etapa fue construir los formularios y el flujo de atención. Después conectamos la aplicación con MySQL mediante JPA/Hibernate y retiramos el almacenamiento temporal en memoria. Los registros se conservan al cerrar y volver a iniciar el sistema.

Luego integramos Spring Security para el acceso por correo y contraseña, las sesiones y los permisos por rol. Los formularios de usuarios y técnicos también muestran errores junto a cada campo y conservan los valores válidos cuando hay algo que corregir.

También comprobamos el primer arranque sobre una base vacía. La aplicación crea únicamente el administrador configurado, permite iniciar sesión con esa cuenta y conserva sus datos después de reiniciar Java y MySQL, incluso al retirar las variables iniciales. Esta comprobación ya forma parte del script de pruebas.

La revisión de la rúbrica confirmó que están cubiertos los ocho criterios de desarrollo técnico. Después ajustamos el formato del código y los comentarios sobre decisiones puntuales. El paquete de fuentes también compiló desde una extracción limpia.

La gestión y el seguimiento están reunidos en **Órdenes de servicio**. Desde esa pantalla se filtran las órdenes y se abre el detalle con el avance, las intervenciones y el historial. Retiramos la entrada de seguimiento que duplicaba el mismo listado.

Actualmente podemos:

- Registrar, consultar, editar y eliminar clientes. La eliminación se rechaza si el cliente tiene equipos asociados.
- Registrar equipos, asociarlos con su cliente y consultar su historial de atención. La identidad del cliente y del equipo se puede corregir mientras no tengan órdenes registradas.
- Crear órdenes, registrar la falla y la prioridad, asignar un técnico y consultar el seguimiento. El personal administrativo puede cambiar al responsable mientras la orden no esté entregada, conservando el avance y dejando constancia en el historial.
- Registrar diagnósticos y trabajos, y avanzar por los estados de atención respetando las reglas del sistema.
- Gestionar técnicos y usuarios, vincular sus cuentas y activar o desactivar su acceso.

El flujo de una orden es:

```text
Registrada → Asignada → En diagnóstico → En atención → Finalizada → Entregada
```

Cada rol tiene sus propias funciones:

| Rol | Funciones |
| --- | --- |
| Administrador | Gestión de usuarios y técnicos. |
| Personal administrativo | Clientes, equipos, recepción, asignación, seguimiento y entrega física. |
| Técnico | Diagnóstico, trabajos y finalización de sus propias órdenes. |

Los formularios de clientes, equipos, órdenes, usuarios y técnicos tienen validaciones del servidor. Al crear una cuenta se exige contraseña; al editarla se puede dejar vacía para conservar la actual. Las peticiones que modifican datos están protegidas con CSRF. Si se desactiva una cuenta o su técnico, su sesión se invalida en la siguiente petición. Los cambios de rol también se aplican en la siguiente petición.

## Reglas de atención y trazabilidad

La recepción se puede corregir mientras la orden está registrada. Cada corrección exige un motivo y conserva en el historial los datos anteriores y los nuevos. La primera asignación cierra la recepción; después se añaden aclaraciones o incidentes sin sobrescribir el ingreso. Las órdenes antiguas conservan el historial que tenían: no se reconstruyen cambios anteriores a estas reglas.

| Operación | Responsable | Condición |
| --- | --- | --- |
| Corregir recepción | Administrativo | Solo registrada, con motivo y versión vigente. |
| Cambiar responsable | Administrativo | Antes de la entrega; técnico habilitado y motivo obligatorio al reasignar. |
| Diagnosticar y registrar trabajos | Técnico asignado | Según la etapa; las revisiones del diagnóstico conservan el texto anterior. |
| Finalizar reparación | Técnico asignado | Debe existir al menos un trabajo. |
| Registrar entrega | Administrativo | Solo finalizada. Requiere nombre y documento de quien recibe en campos separados. |
| Añadir aclaración o incidente | Administrativo o técnico asignado | Se agrega al historial sin modificar datos anteriores; solo antes de entregar. |
| Gestionar cuentas y técnicos | Administrador | No puede cambiar el rol o vínculo de un técnico con órdenes pendientes. Puede desactivar una cuenta para revocar el acceso. |

La identidad de clientes y equipos con órdenes queda bloqueada. El historial registra la fecha y el identificador del usuario junto a su nombre; el repositorio rechaza reemplazar eventos anteriores. Los cambios de órdenes mantienen el control de versiones para evitar sobrescrituras entre ventanas.

Estos controles actúan en la aplicación. Todavía no hay un circuito de aprobación independiente para excepciones, firma del cliente, fotografías ni un registro externo protegido frente a un administrador de MySQL. El nombre y documento de entrega son datos declarados por quien registra la operación.

## Qué falta

El flujo principal ya funciona con MySQL, pero todavía falta cerrar algunos puntos antes de dar por terminado el proyecto:

- Revisar los registros de demostración y pruebas que quedaron en la base de desarrollo, y decidir cuáles retirar antes de trabajar con información definitiva. La aplicación ya no los genera al arrancar.
- Hacer una última revisión de los datos y accesos que se usarán en la demostración.

El informe, los diagramas y la exposición se preparan por separado. Este repositorio contiene el código y los recursos necesarios para ejecutar y probar el sistema. Todavía no se ha validado un despliegue de producción.

## Con qué está construido

Java 17, Spring Boot, Spring MVC, Thymeleaf, Jakarta Validation, Spring Security, JPA/Hibernate, MySQL 8.4 y Bootstrap. Docker se utiliza para ejecutar MySQL.

El código está separado en controladores, servicios, modelos y repositorios. Los repositorios utilizan `EntityManager`; el esquema y sus restricciones están en [database/schema.sql](database/schema.sql). La base tiene ocho tablas: clientes, equipos, técnicos, usuarios, órdenes, diagnósticos, trabajos y eventos de las órdenes.

## Cómo ejecutarlo

Necesitas Java 17, Docker Desktop y PowerShell. El repositorio incluye Maven Wrapper.

En una primera instalación, desde la raíz del proyecto, crea el archivo de configuración local:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
```

Completa `.env` con contraseñas distintas para `MYSQL_ROOT_PASSWORD` y `MYSQL_PASSWORD`. Si la base todavía no tiene usuarios, completa también `JEQUETECH_ADMIN_NAME`, `JEQUETECH_ADMIN_EMAIL` y `JEQUETECH_ADMIN_PASSWORD`. La contraseña inicial debe tener entre 12 y 128 caracteres.

Inicia MySQL y luego la aplicación:

```powershell
docker compose up -d --wait
.\scripts\start-mysql.ps1
```

Abre [http://127.0.0.1:8080/login](http://127.0.0.1:8080/login). En una base sin usuarios, el primer arranque crea únicamente el administrador configurado. Después puedes retirar las tres variables de administrador y crear los demás usuarios desde el sistema.

Para detener la aplicación usa `Ctrl+C`. Para detener MySQL conservando sus datos:

```powershell
docker compose stop
```

El archivo `.env` contiene credenciales locales y queda fuera de Git.

## Cómo comprobar los cambios

La última verificación completa, del **14 de noviembre de 2026**, terminó con **59 pruebas aprobadas**, sin fallos, errores ni omisiones. El recorrido de navegador completó 11 grupos de comprobaciones y generó 28 capturas. Los datos conservaron el mismo hash después de reiniciar la aplicación y MySQL.

Además, se ejecutaron las comprobaciones del primer arranque en una segunda base vacía. La configuración incompleta y la contraseña inicial demasiado corta fueron rechazadas sin crear usuarios. Con una configuración válida se creó un solo administrador y no se insertaron clientes, equipos, técnicos ni órdenes. La cuenta mantuvo su acceso después de reiniciar sin las variables iniciales.

Desde la raíz del proyecto, para repetir las pruebas con una base MySQL temporal:

```powershell
.\scripts\check-persistence.ps1
```

Para incluir el recorrido web, necesitas además Node.js, Playwright y Microsoft Edge:

```powershell
.\scripts\check-persistence.ps1 -Browser
```

Las pruebas cubren reglas de negocio, formularios inválidos, acceso por rol, sesiones, CSRF, eliminación protegida, transacciones, concurrencia y persistencia. Sus registros ficticios se crean únicamente en la base aislada de pruebas y no se incluyen en la aplicación. No habilites `JEQUETECH_MYSQL_TEST` contra la base de trabajo.

Los resultados y capturas se guardan en `exports/verificacion-mysql/`, fuera de Git.

Para compilar sin ejecutar las pruebas:

```powershell
.\mvnw.cmd -B -DskipTests package
```

El archivo ejecutable se genera en `target/jequetech-0.1.0.jar`.

Para la entrega, descarga las fuentes desde GitHub cuando se publique el repositorio. Configura las credenciales locales a partir de `.env.example`.
