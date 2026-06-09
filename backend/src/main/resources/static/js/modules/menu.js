
export function initDrawer() {
  const drawer          = document.getElementById('carrito-drawer');
  const abrirBtn        = document.getElementById('btn-abrir-carrito');
  const abrirBtnMobile  = document.getElementById('btn-abrir-carrito-mobile');
  const cerrarBtn       = document.getElementById('btn-cerrar-carrito');
  const cerrarBtnBg     = document.getElementById('btn-cerrar-carrito-bg');
  const seguirBuscando  = document.getElementById('btn-seguir-buscando');

  if (!drawer) return;

  const abrir  = () => {
    drawer.classList.remove('hidden');
    document.body.classList.add('overflow-hidden');
  };
  const cerrar = () => {
    drawer.classList.add('hidden');
    document.body.classList.remove('overflow-hidden');
  };

  abrirBtn?.addEventListener('click', abrir);
  abrirBtnMobile?.addEventListener('click', abrir);
  document.addEventListener('click', (event) => {
    const opener = event.target.closest('[data-open-cart-drawer]');
    if (!opener) return;
    event.preventDefault();
    abrir();
  });
  cerrarBtn?.addEventListener('click', cerrar);
  cerrarBtnBg?.addEventListener('click', cerrar);
  seguirBuscando?.addEventListener('click', cerrar);
  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && !drawer.classList.contains('hidden')) {
      cerrar();
    }
  });
}

export function initMenuMobile() {
  const btn  = document.getElementById('btn-menu-mobile');
  const menu = document.getElementById('menu-mobile');
  if (!btn || !menu) return;

  btn.addEventListener('click', () => {
    const abierto = menu.classList.toggle('hidden') === false;
    btn.setAttribute('aria-expanded', String(abierto));
  });
}
