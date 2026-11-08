/*
 * Biblioteca SENATI · pruebas funcionales de extremo a extremo.
 *
 * Requisitos:
 *   - Aplicación desplegada en http://localhost:8080/biblioteca
 *   - Base de datos recién inicializada con db/schema.sql y db/seed.sql
 *   - Playwright instalado (npm install playwright && npx playwright install chromium)
 *
 * Uso:
 *   node docs/pruebas/test-flow.cjs
 *   BASE_URL=http://localhost:9090/biblioteca node docs/pruebas/test-flow.cjs
 */
const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const BASE = process.env.BASE_URL || 'http://localhost:8080/biblioteca';
const OUT = process.env.OUT_DIR || path.join(__dirname, '..', 'reportes');
fs.mkdirSync(OUT, { recursive: true });

const results = [];
function check(nombre, ok, detalle = '') {
  results.push({ nombre, ok, detalle });
  console.log(`${ok ? 'PASS' : 'FAIL'} · ${nombre}${detalle ? ' · ' + detalle : ''}`);
}

async function login(page, usuario, clave) {
  await page.goto(`${BASE}/login.xhtml`, { waitUntil: 'networkidle' });
  await page.fill('input[name="loginForm:usuario"]', usuario);
  await page.fill('input[name="loginForm:clave"]', clave);
  await page.click('input[type="submit"]');
  await page.waitForLoadState('networkidle');
}

(async () => {
  const browser = await chromium.launch({ headless: true });
  const ctx = await browser.newContext({ viewport: { width: 1440, height: 950 } });
  const page = await ctx.newPage();
  page.on('pageerror', e => console.log('PAGEERROR', e.message));
  page.on('dialog', d => d.accept());

  // ---------- Autenticación y autorización ----------
  const anon = await browser.newContext();
  const anonPage = await anon.newPage();
  await anonPage.goto(`${BASE}/app/dashboard.xhtml`, { waitUntil: 'networkidle' });
  check('Sin sesión redirige al login', anonPage.url().includes('/login.xhtml'), anonPage.url());
  await anon.close();

  await login(page, 'admin', 'admin123');
  check('Login del bibliotecario', page.url().includes('/app/dashboard.xhtml'), page.url());
  check('Panel con indicadores', (await page.locator('.kpi').count()) === 4);

  // ---------- Libros: validación, alta, edición y baja ----------
  await page.goto(`${BASE}/app/libro-form.xhtml`, { waitUntil: 'networkidle' });
  await page.fill('[id="libroForm:isbn"]', '9780132350885');
  await page.fill('[id="libroForm:titulo"]', 'Libro de prueba QA');
  await page.fill('[id="libroForm:autor"]', 'Equipo QA');
  await page.fill('[id="libroForm:cantidad"]', '2');
  await page.click('#libroForm input[type="submit"]');
  await page.waitForLoadState('networkidle');
  let err = await page.locator('.field-error').first().textContent().catch(() => '');
  check('Validador ISBN rechaza dígito verificador inválido', /verificador|ISBN/i.test(err), err.trim());

  await page.fill('[id="libroForm:isbn"]', '9780306406157');
  await page.fill('[id="libroForm:titulo"]', 'Libro de prueba QA');
  await page.fill('[id="libroForm:autor"]', 'Equipo QA');
  await page.fill('[id="libroForm:cantidad"]', '2');
  await page.click('#libroForm input[type="submit"]');
  await page.waitForLoadState('networkidle');
  check('Alta de libro con ISBN válido', page.url().includes('/app/libros.xhtml') && (await page.locator('table.data').innerText()).includes('Libro de prueba QA'));

  const fila = page.locator('tbody tr', { hasText: 'Libro de prueba QA' }).first();
  await Promise.all([
    page.waitForLoadState('networkidle'),
    fila.locator('a', { hasText: 'Editar' }).click(),
  ]);
  await page.fill('[id="libroForm:titulo"]', 'Libro de prueba QA editado');
  await page.click('#libroForm input[type="submit"]');
  await page.waitForLoadState('networkidle');
  check('Edición de libro', (await page.locator('table.data').innerText()).includes('Libro de prueba QA editado'));

  const filaEditada = page.locator('tbody tr', { hasText: 'Libro de prueba QA editado' }).first();
  await Promise.all([
    page.waitForLoadState('networkidle'),
    filaEditada.locator('input[value="Eliminar"]').click(),
  ]);
  check('Eliminación de libro', !(await page.locator('table.data').innerText()).includes('Libro de prueba QA editado'));

  // ---------- Usuarios ----------
  await page.goto(`${BASE}/app/usuario-form.xhtml`, { waitUntil: 'networkidle' });
  await page.fill('[id="usuarioForm:codigo"]', 'U2026099');
  await page.fill('[id="usuarioForm:nombre"]', 'Usuario de Prueba QA');
  await page.fill('[id="usuarioForm:correo"]', 'usuario.qa@senati.pe');
  await page.click('#usuarioForm input[type="submit"]');
  await page.waitForLoadState('networkidle');
  check('Alta de usuario', (await page.locator('table.data').innerText()).includes('Usuario de Prueba QA'));

  const filaUsuario = page.locator('tbody tr', { hasText: 'Usuario de Prueba QA' }).first();
  await Promise.all([
    page.waitForLoadState('networkidle'),
    filaUsuario.locator('input[value="Eliminar"]').click(),
  ]);
  check('Eliminación de usuario sin préstamos', !(await page.locator('table.data').innerText()).includes('Usuario de Prueba QA'));

  // ---------- API REST antes del préstamo ----------
  let resp = await page.request.get(`${BASE}/api/libros/7/disponibilidad`);
  let dispAntes = await resp.json();
  check('REST disponibilidad responde 200', resp.status() === 200, JSON.stringify(dispAntes));

  // ---------- Préstamo ----------
  await page.goto(`${BASE}/app/prestamo-form.xhtml`, { waitUntil: 'networkidle' });
  await page.fill('[id="prestamoForm:codigo"]', 'U2026001');
  const opcionSistemas = await page.locator('[id="prestamoForm:libro"] option')
    .evaluateAll(opts => opts.find(o => o.textContent.includes('Sistemas de bases de datos'))?.value);
  await page.selectOption('[id="prestamoForm:libro"]', opcionSistemas);
  await page.click('#prestamoForm input[type="submit"]');
  await page.waitForLoadState('networkidle');
  let aviso = await page.locator('.notice-info').first().textContent().catch(() => '');
  check('Registro de préstamo válido', /registrado con éxito/i.test(aviso), aviso.trim());

  resp = await page.request.get(`${BASE}/api/libros/7/disponibilidad`);
  const dispDespues = await resp.json();
  check('El stock disminuye al prestar', dispDespues.ejemplaresDisponibles === dispAntes.ejemplaresDisponibles - 1,
    `${dispAntes.ejemplaresDisponibles} -> ${dispDespues.ejemplaresDisponibles}`);

  await page.goto(`${BASE}/app/prestamos.xhtml`, { waitUntil: 'networkidle' });
  const filaPrestamo = page.locator('tbody tr', { hasText: 'Sistemas de bases de datos' }).first();
  check('El préstamo aparece en el historial', (await filaPrestamo.count()) === 1);

  // ---------- Límite de 3 préstamos ----------
  await page.goto(`${BASE}/app/prestamo-form.xhtml`, { waitUntil: 'networkidle' });
  await page.fill('[id="prestamoForm:codigo"]', 'U2026001');
  const opcionRedes = await page.locator('[id="prestamoForm:libro"] option')
    .evaluateAll(opts => opts.find(o => o.textContent.includes('Redes de computadoras'))?.value);
  await page.selectOption('[id="prestamoForm:libro"]', opcionRedes);
  await page.click('#prestamoForm input[type="submit"]');
  await page.waitForLoadState('networkidle');
  const limite = await page.locator('.field-error, .messages-error').first().textContent().catch(() => '');
  check('Validador de límite bloquea el cuarto préstamo', /3 préstamos|límite/i.test(limite), limite.trim());

  // ---------- Devolución ----------
  await page.goto(`${BASE}/app/prestamos.xhtml`, { waitUntil: 'networkidle' });
  const filaDevolver = page.locator('tbody tr', { hasText: 'Sistemas de bases de datos' })
    .filter({ has: page.locator('input[value="Devolver"]') }).first();
  await Promise.all([
    page.waitForLoadState('networkidle'),
    filaDevolver.locator('input[value="Devolver"]').click(),
  ]);
  const avisoDev = await page.locator('.notice-info').first().textContent().catch(() => '');
  check('Devolución registrada', /Devolución registrada/i.test(avisoDev), avisoDev.trim());

  resp = await page.request.get(`${BASE}/api/libros/7/disponibilidad`);
  const dispFinal = await resp.json();
  check('El stock se repone al devolver', dispFinal.ejemplaresDisponibles === dispAntes.ejemplaresDisponibles,
    `${dispDespues.ejemplaresDisponibles} -> ${dispFinal.ejemplaresDisponibles}`);

  // ---------- Reportes PDF ----------
  for (const [ruta, archivo] of [['/reportes/libros.pdf', 'libros-mas-prestados.pdf'], ['/reportes/mora.pdf', 'morosidad.pdf']]) {
    const r = await page.request.get(`${BASE}${ruta}`);
    const body = await r.body();
    fs.writeFileSync(`${OUT}/${archivo}`, body);
    check(`Reporte ${archivo}`, r.status() === 200 && r.headers()['content-type'].includes('application/pdf') && body.slice(0, 4).toString() === '%PDF',
      `${r.status()} · ${body.length} bytes`);
  }

  // ---------- API REST ----------
  resp = await page.request.get(`${BASE}/api/libros`);
  const libros = await resp.json();
  check('REST catálogo', resp.status() === 200 && libros.length === 10, `${libros.length} libros`);

  resp = await page.request.get(`${BASE}/api/libros/buscar?q=clean`);
  const busqueda = await resp.json();
  check('REST búsqueda', resp.status() === 200 && busqueda.some(l => l.titulo === 'Clean Code'), `${busqueda.length} resultados`);

  resp = await page.request.get(`${BASE}/api/prestamos/mora`);
  const mora = await resp.json();
  check('REST mora', resp.status() === 200 && mora.length >= 1, `${mora.length} registros`);

  // ---------- Estudiante ----------
  const ctxEst = await browser.newContext();
  const est = await ctxEst.newPage();
  await login(est, 'U2026001', '123456');
  check('Login LDAP del estudiante', est.url().includes('/app/mi-cuenta.xhtml'), est.url());
  check('Mi cuenta lista sus préstamos', (await est.locator('table.data').innerText()).includes('Effective Java'));

  await est.goto(`${BASE}/app/reportes.xhtml`, { waitUntil: 'networkidle' });
  check('Estudiante no accede a reportes', est.url().includes('/app/mi-cuenta.xhtml'), est.url());
  await ctxEst.close();

  // ---------- Inactivo ----------
  const ctxIn = await browser.newContext();
  const inactivo = await ctxIn.newPage();
  await login(inactivo, 'U2026003', '123456');
  const errorInactivo = await inactivo.locator('.notice-warn').textContent().catch(() => '');
  check('LDAP rechaza estudiante inactivo', /inactivo|inválidas/i.test(errorInactivo), errorInactivo.trim());
  await ctxIn.close();

  await browser.close();

  const fallos = results.filter(r => !r.ok);
  console.log(`\nResumen: ${results.length - fallos.length}/${results.length} pruebas correctas`);
  if (fallos.length) {
    console.log('Fallos:', fallos.map(f => f.nombre).join(', '));
    process.exitCode = 1;
  }
})();
