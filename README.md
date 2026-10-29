# JequeTech Solutions S.A.C.

JequeTech es un sistema web para organizar las órdenes de un servicio técnico. Permite saber qué equipo ingresó, a quién pertenece, qué falla presenta, quién lo atiende y qué trabajos se realizaron hasta su entrega.

Es un proyecto académico del curso **Marcos de Desarrollo Web**, desarrollado por **Adrian Pisco**. Está en desarrollo y por ahora se ejecuta en un entorno local.

## Qué hemos avanzado

La primera etapa fue construir los formularios y el flujo de atención. Después conectamos la aplicación con MySQL mediante JPA/Hibernate y retiramos el almacenamiento temporal en memoria. Los registros se conservan al cerrar y volver a iniciar el sistema.

Luego integramos Spring Security para el acceso por correo y contraseña, las sesiones y los permisos. El avance más reciente completa la validación de usuarios y técnicos, con errores junto a cada campo y conservación de los datos válidos del formulario.

Actualmente podemos:

- Registrar, consultar, editar y eliminar clientes. La eliminación se rechaza si el cliente tiene equipos asociados.
- Registrar y editar equipos, asociarlos con su cliente y consultar su historial de atención.
- Crear órdenes, registrar la falla y la prioridad, asignar un técnico y consultar el seguimiento.
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
| Personal administrativo | Clientes, equipos, registro y asignación de órdenes, seguimiento e historial. |
| Técnico | Consulta y atención de sus propias órdenes asignadas. |

Los formularios de clientes, equipos, órdenes, usuarios y técnicos tienen validaciones del servidor. Al crear una cuenta se exige contraseña; al editarla se puede dejar vacía para conservar la actual. Las peticiones que modifican datos están protegidas con CSRF. Desactivar una cuenta o su técnico revoca la sesión; cambiar su rol actualiza los permisos en la siguiente petición.

## Qué falta

El flujo principal ya funciona con MySQL, pero todavía falta cerrar algunos puntos antes de dar por terminado el proyecto:

- Probar una instalación desde cero, con la base vacía y un administrador propio. Las pruebas automatizadas actuales preparan sus cuentas en una base aislada.
- Revisar los registros de demostración y pruebas que quedaron en la base de desarrollo, y decidir cuáles retirar antes de trabajar con información definitiva. La aplicación ya no los genera al arrancar.
- Contrastar lo implementado con los requisitos de la entrega final y resolver los ajustes que salgan de esa revisión.

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

La última verificación completa, del **28 de octubre de 2026**, terminó con **49 pruebas aprobadas**, sin fallos, errores ni omisiones. El recorrido de navegador completó 11 grupos de comprobaciones y generó 28 capturas. También se comprobó que los datos conservaran el mismo hash después de reiniciar la aplicación y MySQL.

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

El archivo ejecutable se genera en `target/jequetech-0.1.0.jar`. Para preparar un ZIP de las fuentes sin credenciales ni compilados:

```powershell
.\scripts\package.ps1
```

El paquete queda en `exports/jequetech-fuentes.zip`.
