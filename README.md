# JequeTech

Sistema web para gestionar las órdenes de un servicio técnico. Registra el ingreso de equipos, asigna responsables y reúne los diagnósticos, trabajos y entregas en el historial de cada orden.

Proyecto del curso **Marcos de Desarrollo Web** de la Universidad Tecnológica del Perú (UTP), desarrollado por Adrian Pisco.

## Funciones

| Rol | Qué puede hacer |
| --- | --- |
| Administrador | Gestionar usuarios, técnicos y sus accesos. |
| Administrativo | Registrar clientes y equipos, recibir órdenes, asignar técnicos y registrar la entrega. |
| Técnico | Consultar sus órdenes, registrar diagnósticos y trabajos y finalizar la reparación. |

Una orden pasa por los estados **Registrada → Asignada → En diagnóstico → En atención → Finalizada → Entregada**. La primera asignación cierra la recepción. Si después se cambia de técnico, se exige un motivo y se conserva el avance. El técnico finaliza el trabajo y el administrativo registra la entrega con el nombre y el documento de quien recibe.

## Tecnologías

Java 17, Spring Boot, Spring MVC, Thymeleaf y Bootstrap para la aplicación web. Spring Security gestiona las sesiones y los permisos, y Jakarta Validation valida los formularios en el servidor. Los datos se guardan en MySQL 8.4 con JPA, Hibernate y repositorios que usan EntityManager.

El código está separado en controladores, servicios, modelos y repositorios. El [esquema de MySQL](database/schema.sql) define las relaciones y restricciones de la base.

## Ejecución local

Necesitas Java 17, Docker Desktop y PowerShell. Clona este repositorio o descarga el código desde GitHub. En la carpeta del proyecto, crea la configuración local si todavía no existe:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
```

En `.env`, define contraseñas distintas para `MYSQL_ROOT_PASSWORD` y `MYSQL_PASSWORD`. Para una base nueva, completa también `JEQUETECH_ADMIN_NAME`, `JEQUETECH_ADMIN_EMAIL` y `JEQUETECH_ADMIN_PASSWORD`. La contraseña inicial debe tener entre 12 y 128 caracteres.

```powershell
docker compose up -d --wait
.\scripts\start-mysql.ps1
```

Abre [JequeTech](http://127.0.0.1:8080/login) e inicia sesión con el administrador configurado. Desde esa cuenta puedes crear los usuarios administrativos y técnicos. Para detener la aplicación, pulsa Ctrl+C. Usa `docker compose stop` para detener MySQL conservando sus datos.

## Pruebas

```powershell
.\scripts\check-persistence.ps1
```

El script usa un MySQL temporal y comprueba las validaciones, los permisos, el flujo de órdenes y la persistencia tras reiniciar. La última ejecución completa, el 14 de noviembre de 2026, registró **59 pruebas aprobadas** y **11 grupos de comprobaciones en el navegador**. Para repetir el recorrido web, ejecuta el script con `-Browser` en un entorno con Node.js, Playwright y Microsoft Edge.
