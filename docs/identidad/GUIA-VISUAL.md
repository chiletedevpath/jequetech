# JequeTech Solutions S.A.C. — propuesta de identidad

## Concepto

Monograma geométrico JT y nombre empresarial. La referencia recibida aporta la familia azul/turquesa; se simplifica su composición para mejorar su uso en la aplicación. Las imágenes del Avance 2 son maquetación, no producto final ni identidad obligatoria.

Esta propuesta visual no constituye una comprobación de exclusividad de marca. El PNG generado es un recurso raster; la versión vectorial del logo completo queda pendiente. La aplicación ya incluye un favicon SVG simplificado.

El archivo principal del logo se conserva únicamente en [static/img/jequetech-logo.png](../../src/main/resources/static/img/jequetech-logo.png), junto al [favicon](../../src/main/resources/static/img/favicon.svg). El [prompt de generación](PROMPT.txt) documenta su origen.

## Paleta propuesta

| Uso | Color | HEX |
| --- | --- | --- |
| Marca, navegación y títulos | Azul profundo | `#123B52` |
| Acentos y símbolo | Turquesa | `#16B8B1` |
| Botones principales con texto blanco | Petróleo | `#087F8C` |
| Texto principal | Azul carbón | `#172B3A` |
| Texto secundario | Gris azulado | `#526675` |
| Fondo de aplicación | Gris muy claro | `#F4F8FA` |
| Tarjetas y formularios | Blanco | `#FFFFFF` |

Contraste calculado contra blanco: azul profundo 11.84:1; petróleo 4.75:1; texto principal 14.55:1; texto secundario 5.97:1. El turquesa tiene 2.46:1 contra blanco: reservarlo para acentos decorativos, no texto pequeño ni fondos de botones con texto blanco. Estos valores corresponden a los HEX especificados, no a una medición de los píxeles del PNG generado.

## Aplicación

- Login: logo sobre blanco, título azul profundo y botón petróleo.
- Navegación: fondo claro para el logo principal; preparar una variante invertida antes de usarlo sobre fondo oscuro.
- Formularios y tablas: fondos claros, texto azul carbón y bordes discretos.
- Estados de órdenes: combinar siempre etiqueta textual con el color; la paleta de marca no sustituye los estados semánticos.
- Mantener proporciones y margen libre alrededor del logo. No añadir sombras ni deformarlo.

## Origen e integración

El logo está integrado en el acceso y la navegación de Spring Boot y la paleta se aplica al sistema. El alcance académico y las verificaciones de la implementación se documentan en [Alcance APF2](../ALCANCE-APF2.md) y [Verificación](../VERIFICACION.md).

Generación realizada mediante la herramienta integrada image_gen, con la habilidad imagegen. La imagen adjunta se usó como referencia de nombre y familia cromática.
