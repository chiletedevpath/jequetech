# JequeTech Solutions S.A.C.

Sistema web para la gestión y seguimiento de órdenes de servicio técnico, desarrollado para el curso **Marcos de Desarrollo Web**.

El proyecto permite registrar clientes y equipos, crear órdenes de servicio, asignar técnicos, registrar diagnósticos y trabajos realizados, controlar los estados de atención y consultar el historial de cada equipo.

Actualmente el desarrollo se encuentra en el cierre técnico del **APF3**.

---

## Estado actual del proyecto

Hasta el momento se ha completado la integración entre:

- Spring Boot
- Spring MVC
- Thymeleaf
- Jakarta Validation
- JPA / Hibernate
- MySQL
- Bootstrap
- Docker

La aplicación puede ejecutarse con dos perfiles:

- `memoria`: utilizado principalmente para pruebas rápidas.
- `mysql`: utilizado para trabajar con persistencia real.

Para el APF3, la persistencia principal utilizada es **MySQL mediante JPA/Hibernate**.

---

## Funcionalidades implementadas

### Clientes

Se encuentra implementado el CRUD completo:

- Registrar cliente.
- Listar clientes.
- Editar cliente.
- Eliminar cliente.

La eliminación está controlada por una regla de negocio:

> Un cliente no puede eliminarse si tiene equipos registrados.

Esto permite evitar problemas de integridad con los equipos y órdenes asociadas.

---

### Equipos

Actualmente permite:

- Registrar equipos.
- Consultar equipos.
- Editar equipos.
- Asociar cada equipo con su cliente.
- Consultar el historial de órdenes de un equipo.

---

### Órdenes de servicio

El sistema permite:

- Registrar una orden.
- Seleccionar cliente y equipo.
- Registrar la falla reportada.
- Establecer prioridad.
- Asignar técnico.
- Registrar diagnóstico.
- Registrar trabajos realizados.
- Actualizar el estado de atención.
- Consultar el seguimiento de la orden.

El flujo utilizado es:

```text
Registrada
   ↓
Asignada
   ↓
En diagnóstico
   ↓
En atención
   ↓
Finalizada
   ↓
Entregada
```

---

### Técnicos y usuarios

Se encuentra implementado:

- Registro de técnicos.
- Gestión de usuarios.
- Vinculación de usuarios técnicos.
- Activación y desactivación de cuentas.
- Control de acceso según rol.

Roles utilizados:

- Administrador
- Personal administrativo
- Técnico

---

## Validaciones

Durante el APF3 se incorporó **Spring Validator / Jakarta Validation** en los formularios principales.

### Cliente

Se validan:

- nombres obligatorios;
- longitud de nombres y apellidos;
- documento obligatorio;
- formato del documento;
- teléfono obligatorio;
- formato del teléfono;
- correo obligatorio;
- formato del correo.

### Equipo

Se validan:

- cliente obligatorio;
- tipo;
- marca;
- modelo;
- número de serie.

### Orden de servicio

Se validan:

- cliente;
- equipo;
- fecha de recepción;
- falla reportada;
- prioridad;
- observaciones.

También se controla que la fecha de recepción no sea futura.

Los errores se muestran directamente en los formularios mediante Thymeleaf.

---

## Base de datos

La base de datos utilizada es:

```text
MySQL 8.4
```

Nombre de la base:

```text
jequetech
```

Actualmente se utilizan ocho tablas principales:

```text
cliente
equipo
tecnico
usuario
orden_servicio
diagnostico
trabajo_realizado
evento_orden
```

Las relaciones principales son:

```text
Cliente
   |
   └── Equipo
          |
          └── Orden de servicio
                  |
                  ├── Técnico
                  ├── Diagnóstico
                  ├── Trabajos realizados
                  └── Eventos / historial
```

El esquema se encuentra en:

```text
database/schema.sql
```

---

## Persistencia con JPA / Hibernate

La persistencia MySQL se implementó utilizando JPA e Hibernate.

El proyecto mantiene la separación entre:

```text
Controller
   ↓
Service
   ↓
Repository
   ↓
JPA / Hibernate
   ↓
MySQL
```

Los repositorios JPA utilizan `EntityManager`.

También se mantiene una implementación en memoria para pruebas y ejecución alternativa.

---

## Pruebas realizadas

Actualmente la suite contiene:

```text
32 pruebas
```

Distribuidas de la siguiente manera:

```text
AplicacionTest          17 pruebas
CrudValidacionTest       9 pruebas
PersistenciaMySqlTest    6 pruebas
```

Resultado de la última verificación completa con MySQL:

```text
Tests run: 32
Failures: 0
Errors: 0
Skipped: 0
```

También se verificó:

- creación de registros;
- actualización;
- eliminación;
- reglas de negocio;
- formularios inválidos;
- rollback;
- persistencia después de reiniciar la aplicación;
- persistencia después de reiniciar MySQL;
- recorrido funcional desde navegador.

---

## Verificación del CRUD de clientes

Para comprobar la eliminación se realizaron dos casos.

### Cliente sin equipos

Resultado:

```text
Cliente eliminado correctamente.
```

La consulta posterior en MySQL confirmó que el registro ya no existía.

### Cliente con equipos

Resultado:

```text
No se puede eliminar un cliente con equipos registrados.
```

El cliente permaneció almacenado correctamente.

---

## Ejecutar el proyecto con MySQL

### 1. Requisitos

Se necesita:

- Java 17 o superior compatible;
- Docker Desktop;
- PowerShell.

El proyecto incluye Maven Wrapper, por lo que no es necesario instalar Maven manualmente.

---

### 2. Configurar variables locales

Crear el archivo:

```text
.env
```

tomando como referencia:

```text
.env.example
```

Las contraseñas locales no deben subirse al repositorio.

---

### 3. Iniciar MySQL y la aplicación

Desde la raíz del proyecto:

```powershell
.\scripts\start-mysql.ps1
```

Luego abrir:

```text
http://localhost:8080/login
```

---

## Usuarios de demostración

Las cuentas ficticias utilizadas durante las pruebas son:

| Usuario                    | Rol                     |
| -------------------------- | ----------------------- |
| `admin@jequetech.test`     | Administrador           |
| `recepcion@jequetech.test` | Personal administrativo |
| `tecnico@jequetech.test`   | Técnico                 |
| `ana@jequetech.test`       | Técnico                 |

Contraseña de demostración:

```text
Demo2026!
```

Estas credenciales solo pertenecen al entorno académico del proyecto.

---

## Ejecutar las pruebas

### Pruebas generales

```powershell
.\mvnw.cmd verify
```

En una ejecución normal, las pruebas exclusivas de MySQL pueden aparecer como omitidas porque requieren un entorno MySQL aislado.

---

### Pruebas completas con MySQL

```powershell
.\scripts\check-persistence.ps1
```

Este script ejecuta la verificación utilizando MySQL real.

Resultado esperado:

```text
32 pruebas
0 fallos
0 errores
0 omitidas
```

---

### Verificación con navegador

Si se dispone de Node.js, Playwright y Microsoft Edge:

```powershell
.\scripts\check-persistence.ps1 -Browser
```

En la última ejecución se realizaron:

```text
10 grupos de pruebas
26 capturas
```

Las evidencias generadas se almacenan localmente en:

```text
exports/
```

Esta carpeta no se versiona en Git.

---

## Estructura principal

```text
src/
├── main/
│   ├── java/pe/jequetech/
│   │   ├── config/
│   │   ├── controller/
│   │   │   └── form/
│   │   ├── model/
│   │   ├── repository/
│   │   │   ├── jpa/
│   │   │   └── memory/
│   │   └── service/
│   │
│   └── resources/
│       ├── templates/
│       └── static/
│
├── test/
│   └── java/pe/jequetech/
│
database/
├── schema.sql
└── tests/

scripts/

docs/
```

---

## Avances realizados

### APF2

Se desarrolló la base funcional del sistema:

- interfaz web;
- clientes;
- equipos;
- técnicos;
- usuarios;
- órdenes;
- diagnóstico;
- trabajos;
- seguimiento;
- historial;
- roles y sesiones.

---

### APF3

Hasta el momento se completó:

- esquema relacional MySQL;
- relaciones y restricciones;
- integración JPA/Hibernate;
- persistencia real;
- transacciones;
- validaciones con Jakarta Validation;
- mensajes de error en formularios;
- CRUD completo de clientes;
- eliminación controlada;
- pruebas de reglas de negocio;
- pruebas de rollback;
- pruebas de persistencia MySQL;
- pruebas de navegador;
- verificación de datos después de reinicios.

---

## Pendiente

La parte técnica principal del APF3 se encuentra terminada.

Queda pendiente principalmente la documentación académica:

- actualizar el caso de uso CU02 con la eliminación controlada;
- actualizar diagramas UML;
- ajustar el diagrama de secuencia con Validator, JPA y MySQL;
- incorporar el modelo de base de datos;
- incorporar el script SQL;
- seleccionar las capturas para el informe;
- documentar las pruebas realizadas;
- redactar las conclusiones;
- preparar los anexos del APF3.

Para el proyecto final todavía quedarán etapas posteriores del curso, entre ellas la revisión de seguridad y los requisitos que indique el docente.

---

## Última verificación

Fecha:

```text
24 de octubre de 2026
```

Estado:

```text
Compilación:          BUILD SUCCESS
Pruebas generales:    PASS
Pruebas MySQL:         PASS
CRUD clientes:         PASS
Validator:             PASS
Persistencia:          PASS
Rollback:              PASS
Recorrido navegador:   PASS
```

Estado técnico del APF3:

```text
LISTO PARA DOCUMENTACIÓN
```

---

## Autor

Proyecto académico desarrollado por **Adrian Pisco** para el curso **Marcos de Desarrollo Web**.

https://chiletedevpath.com/
