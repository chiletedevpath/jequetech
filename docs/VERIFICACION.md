# Verificación técnica

Desde la raíz del proyecto, con Java 17, PowerShell y Docker Desktop:

```powershell
.\scripts\check-persistence.ps1
```

El script crea una base MySQL temporal, ejecuta las pruebas y comprueba el primer arranque y la conservación de datos tras reiniciar MySQL y la aplicación. Al terminar elimina el contenedor y el volumen de prueba.

Para probar el flujo en Microsoft Edge, instala Node.js y Playwright y añade el parámetro `-Browser`:

```powershell
npm install --prefix ./exports/browser-deps --no-save --no-package-lock playwright
$env:PLAYWRIGHT_MODULE = (Resolve-Path ./exports/browser-deps/node_modules/playwright).Path
.\scripts\check-persistence.ps1 -Browser
```

Las pruebas cubren el CRUD de clientes, las validaciones de formularios, los permisos por rol, la recepción y la entrega, el historial de órdenes y las actualizaciones concurrentes. El recorrido web también comprueba los errores 403 y 404 y la navegación en cuatro tamaños de pantalla.

El resumen queda en `exports/verificacion-mysql/resultado.json`. El informe y las capturas del navegador quedan en `exports/verificacion-mysql/navegador/`. Para comprobar únicamente las restricciones del esquema, ejecuta `.\scripts\check-database.ps1`.
