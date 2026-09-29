# Alcance del Avance 2

Fuentes revisadas: Avance2_Marcos de Desarrollo Web.docx actualizado, rúbrica APF2, rúbrica del proyecto final, sílabo 2026-2 y los cuatro diagramas actualizados suministrados por el autor. Las instrucciones operativas proceden de la solicitud del usuario y su texto adjunto; los documentos académicos se usan como especificación y evidencia del alcance.

## Trazabilidad funcional

| Caso | Implementación del avance |
| --- | --- |
| CU-01 | Acceso por correo/contraseña, sesión y navegación por rol |
| CU-02 | Registrar, consultar y editar clientes |
| CU-03 | Registrar, consultar y editar equipos de un cliente |
| CU-04 | Registrar orden en estado Registrada; formulario → controller → service → repository → vista |
| CU-05 | Consulta de órdenes con búsqueda por código, cliente, equipo/serie y filtro de estado |
| CU-06 | Asignación independiente; transición a Asignada |
| CU-07 | Listado y detalle limitado al técnico responsable |
| CU-08 | Guardar diagnóstico; transición a En diagnóstico |
| CU-09 | Registrar múltiples trabajos; primer trabajo lleva a En atención |
| CU-10 | Modal de confirmación, secuencia válida y restricciones de avance |
| CU-11 | Seguimiento: estado, responsable, diagnóstico, trabajos e historial |
| CU-12 | Cuentas con rol, estado, contraseña y vínculo de técnico |
| CU-13 | Directorio de técnicos con especialidad, teléfono y estado |
| CU-14 | Historial por identificador de equipo, incluida respuesta sin atenciones |

## Decisiones respecto del documento actualizado

- Las maquetas determinan la organización y los flujos, no el acabado final. La identidad propia utiliza el logo JT y la paleta azul/petróleo/turquesa.
- La versión actualizada del CU-04 y la secuencia prevalecen sobre la maqueta antigua que mostraba un técnico en el formulario inicial. La asignación ocurre después del registro.
- `OrdenServicioController`, `OrdenServicioService` y `OrdenServicioRepository` corresponden a los componentes del diagrama. Tras registrar, se redirige al detalle de la nueva orden para facilitar su asignación, en lugar del listado dibujado en la secuencia. Se conserva el patrón POST–Redirect–GET y el mensaje de confirmación.
- `TrabajoRealizado` está separado de `Diagnostico`; hay como máximo un diagnóstico y varios trabajos por orden, conforme al UML actualizado.
- El vínculo usuario–técnico es único. Se registra primero el técnico y luego se crea su cuenta; solo se puede asignar trabajo a un técnico activo con cuenta habilitada. La etapa intermedia de registro aún sin cuenta permite completar ambos formularios sin crear datos ficticios implícitos.
- El estado de usuario y técnico se representa mediante un booleano `activo`, con etiquetas Activo/Inactivo en la interfaz; los estados de orden, prioridades y roles son enums para impedir valores arbitrarios.
- Los objetos del dominio son records inmutables. Los adaptadores JPA futuros deberán mapearlos a entidades persistentes convencionales. No son entidades JPA ya implementadas.
- El historial de eventos es un valor auxiliar de trazabilidad del agregado orden, no un módulo adicional. Los identificadores de trabajos son locales a su orden en este adaptador; el mapeo persistente definirá sus claves.
- El código protege contra actualizaciones de una orden desde una versión desactualizada. La memoria y sus bloqueos son para una instancia local; no sustituyen transacciones o control de concurrencia en MySQL.
- Las transiciones son secuenciales. Se exige diagnóstico antes de atención y al menos un trabajo antes de finalizar. Los botones muestran únicamente el siguiente paso habilitado; el servidor vuelve a comprobarlo.
- Las fechas iniciales se calculan al arrancar la demostración y al abrir el formulario. Los datos de clientes, equipos, técnicos e indicadores proceden de Java, no de filas escritas en las plantillas.

## Límites de esta etapa

La rúbrica APF2 evalúa Spring/Thymeleaf (6 puntos), Bootstrap responsive (5), exposición (4), informe Word (3) y estructura/integración/creatividad (2). La implementación aborda los criterios técnicos; la exposición y el Word requieren trabajo y evaluación académica independientes.

El sílabo ubica persistencia, JPA/Hibernate, MySQL, CRUD y validación formal en semanas 9–12. Spring Security y autenticación/autorización formal corresponden a semanas 13–16; la presentación final se sitúa en semanas 17–18. No se adelantaron dependencias de persistencia ni JWT.

No se incorporaron facturación, pagos, contabilidad, inventario comercial, WhatsApp, servicios externos ni aplicaciones móviles nativas. Tampoco se modificaron los documentos Word proporcionados.
