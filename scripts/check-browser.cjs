const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || "playwright");
const base = process.env.BASE_URL || "http://127.0.0.1:8080";
const out = path.resolve(process.env.EVIDENCE_DIR || "exports/evidencias");
fs.mkdirSync(out, { recursive: true });
const stamp = Date.now().toString();
const summary = { base, screenshots: [], checks: [], failures: [] };
let browser;
async function capture(page, name) {
  await page.evaluate(() =>
    Promise.all(
      document
        .getAnimations()
        .map((animation) => animation.finished.catch(() => {})),
    ),
  );
  await page.screenshot({
    path: path.join(out, name + ".png"),
    fullPage: !name.includes("modal") && !name.startsWith("menu-"),
    animations: "disabled",
  });
  summary.screenshots.push(name + ".png");
}
async function visit(page, route) {
  const response = await page.goto(base + route);
  assert.equal(response.status(), 200, route);
}
async function login(context, email) {
  const page = await context.newPage();
  page.setDefaultTimeout(10000);
  page.on("pageerror", (e) => summary.failures.push(e.message));
  page.on("response", (r) => {
    if (r.status() >= 500) summary.failures.push(r.status() + " " + r.url());
  });
  await visit(page, "/login");
  await page.locator("#correo").fill(email);
  await page.locator("#clave").fill("Demo2026!");
  await page.getByRole("button", { name: "Ingresar al sistema" }).click();
  await page.waitForURL("**/dashboard");
  return page;
}
async function save(page) {
  await page
    .getByRole("button", { name: "Guardar cambios", exact: true })
    .click();
  await page.waitForLoadState();
  assert.match(
    await page.locator("body").innerText(),
    /(guardad|actualizad)[oa]s? correctamente/i,
  );
}
async function changeState(page) {
  await page
    .getByRole("button", { name: "Actualizar estado", exact: true })
    .click();
  await page.locator("#cambiarEstado").waitFor({ state: "visible" });
  await page
    .getByRole("button", { name: "Confirmar cambio", exact: true })
    .click();
  await page.waitForLoadState();
}
(async () => {
  browser = await chromium.launch({
    headless: true,
    channel: process.env.BROWSER_CHANNEL || "msedge",
  });
  const receptionContext = await browser.newContext({
    viewport: { width: 1440, height: 1000 },
  });
  const p = await login(receptionContext, "recepcion@jequetech.test");
  await capture(p, "01-dashboard-desktop");
  await visit(p, "/clientes/nuevo");
  await p.locator("#nombres").fill("Cliente prueba " + stamp);
  await p.locator("#apellidos").fill("Demostración");
  await p.locator("#documento").fill("T" + stamp);
  await p.locator("#telefono").fill("900000099");
  await p.locator("#correo").fill("prueba" + stamp + "@example.test");
  await save(p);
  const row = p.getByRole("row").filter({ hasText: "T" + stamp });
  const editClient = await row
    .getByRole("link", { name: "Editar" })
    .getAttribute("href");
  const clientId = editClient.match(/\/(\d+)\/editar/)[1];
  await row.getByRole("link", { name: "Editar" }).click();
  await p.locator("#telefono").fill("900000098");
  await save(p);
  summary.checks.push("Crear y editar cliente");
  await visit(p, "/equipos/nuevo");
  await p.locator("#clienteId").selectOption(clientId);
  await p.locator("#tipo").fill("Laptop");
  await p.locator("#marca").fill("Demo");
  await p.locator("#modelo").fill("Prueba");
  await p.locator("#numeroSerie").fill("SERIE-" + stamp);
  await save(p);
  const erow = p.getByRole("row").filter({ hasText: "SERIE-" + stamp });
  const editEquipment = await erow
    .getByRole("link", { name: "Editar" })
    .getAttribute("href");
  const equipmentId = editEquipment.match(/\/(\d+)\/editar/)[1];
  await erow.getByRole("link", { name: "Historial" }).click();
  assert.match(await p.locator("body").innerText(), /aún no tiene historial/);
  await visit(p, editEquipment);
  await p.locator("#modelo").fill("Prueba actualizada");
  await save(p);
  summary.checks.push("Crear y editar equipo; historial vacío");
  await visit(p, "/ordenes/nueva");
  assert.match(
    await p.locator("#fechaRecepcion").inputValue(),
    /^\d{4}-\d{2}-\d{2}$/,
  );
  await p.locator("#clienteId").selectOption(clientId);
  assert.equal(await p.locator("#equipoId option").count(), 2);
  await p.locator("#equipoId").selectOption(equipmentId);
  await p
    .locator("#fallaReportada")
    .fill("No enciende. Prueba integral " + stamp);
  await p.locator("#prioridad").selectOption("ALTA");
  await p
    .locator("#observaciones")
    .fill("Se recibe con cargador. Datos ficticios.");
  await capture(p, "02-nueva-orden");
  await p.getByRole("button", { name: "Registrar orden", exact: true }).click();
  await p.waitForURL(/\/ordenes\/\d+$/);
  const orderRoute = new URL(p.url()).pathname;
  assert.equal(
    await p.locator(".page-heading .status").innerText(),
    "Registrada",
  );
  await p.getByRole("link", { name: "Editar recepción" }).click();
  await p.locator("#observaciones").fill("Recepción verificada en navegador.");
  await save(p);
  await p.locator("#tecnicoId").selectOption("1");
  await p.getByRole("button", { name: "Asignar técnico", exact: true }).click();
  await p.waitForLoadState();
  assert.equal(
    await p.locator(".page-heading .status").innerText(),
    "Asignada",
  );
  summary.checks.push(
    "Registrar, editar y asignar orden con equipo del cliente",
  );
  const technicalContext = await browser.newContext({
    viewport: { width: 1440, height: 1000 },
  });
  const t = await login(technicalContext, "tecnico@jequetech.test");
  await visit(t, orderRoute);
  await t.locator("#diagnostico").fill("Conector de alimentación averiado.");
  await t.getByRole("button", { name: "Guardar diagnóstico" }).click();
  await t.waitForLoadState();
  assert.equal(
    await t.locator(".page-heading .status").innerText(),
    "En diagnóstico",
  );
  await t
    .locator("#trabajo")
    .fill("Reemplazo del conector y pruebas de encendido.");
  await t.getByRole("button", { name: "Registrar trabajo" }).click();
  await t.waitForLoadState();
  assert.equal(
    await t.locator(".page-heading .status").innerText(),
    "En atención",
  );
  await t
    .getByRole("button", { name: "Actualizar estado", exact: true })
    .click();
  await t.locator("#cambiarEstado").waitFor({ state: "visible" });
  await capture(t, "03-modal-confirmacion");
  await t.getByRole("button", { name: "Cancelar", exact: true }).click();
  await t.locator("#cambiarEstado").waitFor({ state: "hidden" });
  assert.equal(
    await t.locator(".page-heading .status").innerText(),
    "En atención",
  );
  await changeState(t);
  assert.equal(
    await t.locator(".page-heading .status").innerText(),
    "Finalizada",
  );
  await changeState(t);
  assert.equal(
    await t.locator(".page-heading .status").innerText(),
    "Entregada",
  );
  await capture(t, "04-orden-entregada");
  summary.checks.push(
    "Diagnóstico → trabajos → modal cancelar/confirmar → finalizada → entregada",
  );
  await visit(p, "/equipos/" + equipmentId + "/historial");
  assert.equal(await p.locator("tbody tr").count(), 1);
  await capture(p, "05-historial-equipo");
  await visit(p, "/ordenes?q=NO-EXISTE-" + stamp);
  assert.match(
    await p.locator("body").innerText(),
    /No se encontraron órdenes/,
  );
  await visit(p, "/ordenes?estado=ENTREGADA");
  assert.ok(
    (await p.locator("tbody .status").allTextContents()).every(
      (x) => x === "Entregada",
    ),
  );
  summary.checks.push("Historial exacto, búsqueda vacía y filtro por estado");
  await visit(p, "/clientes");
  p.once("dialog", (dialog) => dialog.accept());
  await p.locator(`form[action='/clientes/${clientId}/eliminar'] button`).click();
  await p.getByText("No se puede eliminar un cliente con equipos registrados.").waitFor();
  await capture(p, "apf3-eliminacion-protegida");
  await visit(p, "/clientes/nuevo");
  await p.locator("#nombres").fill("Cliente eliminable " + stamp);
  await p.locator("#documento").fill("D" + stamp);
  await p.locator("#telefono").fill("900000099");
  await p.locator("#correo").fill("eliminable" + stamp + "@example.test");
  await save(p);
  const orphanRow = p.getByRole("row").filter({ hasText: "D" + stamp });
  p.once("dialog", (dialog) => dialog.accept());
  await orphanRow.getByRole("button", { name: "Eliminar" }).click();
  await p.getByText("Cliente eliminado correctamente.").waitFor();
  assert.equal(await p.getByRole("row").filter({ hasText: "D" + stamp }).count(), 0);
  await capture(p, "apf3-cliente-eliminado");
  summary.checks.push("DELETE físico de cliente y protección de cliente con equipo");
  await visit(p, "/clientes/nuevo");
  await p.locator("#nombres").fill("Valor conservado");
  await p.locator("#documento").fill("incorrecto!");
  await p.locator("#telefono").fill("900000099");
  await p.locator("#correo").fill("validacion@example.test");
  // Desactivar solo la validación nativa para comprobar la respuesta del servidor.
  await p.locator("form").filter({ has: p.locator("#nombres") }).evaluate((form) => { form.noValidate = true; });
  await p.getByRole("button", { name: "Guardar cambios" }).click();
  await p.locator("#documento.is-invalid").waitFor();
  assert.equal(await p.locator("#nombres").inputValue(), "Valor conservado");
  await capture(p, "apf3-validator-servidor");
  summary.checks.push("Validator del servidor muestra errores y conserva valores");
  const adminContext = await browser.newContext({
    viewport: { width: 1440, height: 1000 },
  });
  const a = await login(adminContext, "admin@jequetech.test");
  await visit(a, "/tecnicos/nuevo");
  await a.locator("#nombres").fill("Técnico prueba " + stamp);
  await a.locator("#especialidad").fill("Hardware");
  await a.locator("#telefono").fill("900000088");
  await save(a);
  const tr = a.getByRole("row").filter({ hasText: "Técnico prueba " + stamp });
  const editTech = await tr
    .getByRole("link", { name: "Editar" })
    .getAttribute("href");
  const techId = editTech.match(/\/(\d+)\/editar/)[1];
  await tr.getByRole("link", { name: "Editar" }).click();
  await a.locator("#especialidad").fill("Hardware y redes");
  await save(a);
  await visit(a, "/usuarios/nuevo");
  await a.locator("#nombre").fill("Usuario prueba " + stamp);
  const newEmail = "tecnico" + stamp + "@example.test";
  await a.locator("#correo").fill(newEmail);
  await a.locator("#clave").fill("Demo2026!");
  await a.locator("#rol").selectOption("TECNICO");
  await a.locator("#tecnicoId").selectOption(techId);
  await save(a);
  const ur = a.getByRole("row").filter({ hasText: newEmail });
  const editUser = await ur
    .getByRole("link", { name: "Editar" })
    .getAttribute("href");
  const newContext = await browser.newContext();
  const n = await login(newContext, newEmail);
  await visit(n, "/ordenes");
  assert.match(
    await n.locator("body").innerText(),
    /No se encontraron órdenes/,
  );
  await visit(a, editUser);
  await a.locator("#activo").selectOption("false");
  await save(a);
  await n.goto(base + "/dashboard");
  assert.ok(n.url().endsWith("/login"));
  summary.checks.push(
    "Crear/editar técnico y usuario; técnico sin órdenes; cuenta inactiva revoca sesión",
  );
  for (const [page, route, expected] of [
    [t, "/usuarios", 403],
    [t, "/ordenes/2", 403],
    [a, "/clientes", 403],
    [p, "/ordenes/9999999", 404],
  ]) {
    const r = await page.goto(base + route);
    assert.equal(r.status(), expected);
    assert.ok(
      await page.getByRole("link", { name: "Ir al panel" }).isVisible(),
    );
  }
  summary.checks.push("403 y 404 con página de recuperación");
  // Revisión de toda la navegación en cuatro tamaños, sin desbordamiento de página.
  for (const width of [390, 768, 1366, 1920]) {
    await p.setViewportSize({ width, height: 900 });
    for (const [route, name] of [
      ["/dashboard", "dashboard"],
      ["/ordenes", "ordenes"],
      ["/ordenes/nueva", "formulario"],
      [orderRoute, "detalle"],
      ["/clientes", "clientes"],
      ["/equipos", "equipos"],
      ["/seguimiento", "seguimiento"],
    ]) {
      await visit(p, route);
      assert.ok(
        await p.evaluate(
          () => document.documentElement.scrollWidth <= innerWidth + 1,
        ),
        `Overflow ${width} ${route}`,
      );
      if (["dashboard", "formulario", "detalle"].includes(name))
        await capture(p, `${name}-${width}`);
    }
    if (width < 992) {
      await p.getByRole("button", { name: "Abrir menú" }).click();
      await p.locator("#menu").waitFor({ state: "visible" });
      await capture(p, "menu-" + width);
      await p.getByRole("button", { name: "Cerrar menú" }).click();
      await p.getByRole("button", { name: "Abrir menú" }).click();
      await p
        .locator("#menu")
        .getByRole("link", { name: "Clientes", exact: true })
        .click();
      await p.waitForURL("**/clientes");
    }
    await a.setViewportSize({ width, height: 900 });
    for (const route of [
      "/usuarios",
      "/usuarios/nuevo",
      "/tecnicos",
      "/tecnicos/nuevo",
    ]) {
      await visit(a, route);
      assert.ok(
        await a.evaluate(
          () => document.documentElement.scrollWidth <= innerWidth + 1,
        ),
        `Overflow ${width} ${route}`,
      );
    }
    const ctx = await browser.newContext({ viewport: { width, height: 900 } });
    const l = await ctx.newPage();
    await visit(l, "/login");
    assert.ok(
      await l.evaluate(
        () => document.documentElement.scrollWidth <= innerWidth + 1,
      ),
    );
    await capture(l, "login-" + width);
    await ctx.close();
  }
  summary.checks.push(
    "Responsive: 390, 768, 1366 y 1920 px; menú móvil; formularios y tablas",
  );
  assert.deepEqual(summary.failures, []);
  summary.result = "PASS";
  console.log(JSON.stringify(summary, null, 2));
})()
  .catch((e) => {
    summary.result = "FAIL";
    summary.failures.push(e.stack);
    console.error(e);
    process.exitCode = 1;
  })
  .finally(async () => {
    fs.writeFileSync(
      path.join(out, "resultado-navegador.json"),
      JSON.stringify(summary, null, 2),
    );
    if (browser) await browser.close();
  });
