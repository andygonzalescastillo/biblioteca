
import { showToast } from './toast.js';
import { initIcons } from './icons.js';
import { initCarritoPage } from './carrito-page.js';

export function initCarritoAjax() {
  document.addEventListener('submit', function (e) {
    const form = e.target.closest('form.form-carrito-ajax');
    if (!form) return;
    e.preventDefault();

    const url       = form.action;
    const ctx       = form.dataset.context || '';
    const submitBtn = form.querySelector('button[type="submit"]');

    
    const isAdding = url.includes('/portal/carrito/libros/') && !url.includes('/reducir') && !url.includes('/quitar');
    if (isAdding) {
      const el = document.getElementById('cupo-libre-badge');
      if (el) {
        const currentCupo = parseInt(el.textContent, 10);
        if (currentCupo <= 0) {
          showToast('No puedes agregar más libros. Has alcanzado el límite máximo de cupo de préstamos permitidos.', 'error');
          if (submitBtn) submitBtn.disabled = false;
          return;
        }
      }
    }

    
    if (submitBtn) submitBtn.disabled = true;

    fetch(url, {
      method: 'POST',
      headers: {
        'X-Requested-With': 'XMLHttpRequest',
        'X-App-Context': ctx
      },
    })
      .then(async (response) => {
        if (!response.ok) {
          const errorMsg =
            response.headers.get('X-Error-Message') ||
            'Ocurrió un error al actualizar el carrito.';
          showToast(errorMsg, 'error');
          if (submitBtn) submitBtn.disabled = false;
          return;
        }

        const html = await response.text();

        
        if (ctx === 'carrito') {
          const content = document.getElementById('carrito-contenido-dinamico');
          if (content) {
            content.innerHTML = html;
            initIcons();
            initCarritoPage();
          }
        } else {
          
          const drawerLista = document.getElementById('drawer-lista-libros');
          if (drawerLista) {
            drawerLista.innerHTML = html;
            initIcons();
          }
        }

        
        let totalCantidad = 0;
        if (ctx === 'carrito') {
          document.querySelectorAll('#carrito-contenido-dinamico output.text-stone-950').forEach((output) => {
            totalCantidad += parseInt(output.textContent, 10);
          });
        } else {
          document.querySelectorAll('#drawer-lista-libros span.bg-indigo-600\\/10').forEach((badge) => {
            const match = badge.textContent.match(/(\d+)\s*ud/);
            if (match) totalCantidad += parseInt(match[1], 10);
          });
        }

        
        ['carrito-badge', 'carrito-badge-mobile'].forEach((id) => {
          const el = document.getElementById(id);
          if (!el) return;
          el.textContent = totalCantidad;
          el.classList.toggle('hidden', totalCantidad === 0);
        });
        const drawerBadge = document.getElementById('carrito-badge-drawer');
        if (drawerBadge) drawerBadge.textContent = totalCantidad;

        
        ['cupo-libre-badge', 'cupo-libre-badge-mobile'].forEach((id) => {
          const el = document.getElementById(id);
          if (!el) return;
          const baseCupo = parseInt(el.dataset.baseCupo || '0', 10);
          el.textContent = Math.max(0, baseCupo - totalCantidad);
        });

        
        const resumenCant = document.getElementById('resumen-cantidad');
        if (resumenCant) resumenCant.textContent = totalCantidad;

        
        const resumenCupoEl = document.getElementById('resumen-cupo-libre');
        if (resumenCupoEl) {
          const baseCupo = parseInt(resumenCupoEl.dataset.baseCupo || '0', 10);
          const maximo   = parseInt(resumenCupoEl.dataset.maximo   || '1',  10);
          const nuevo    = Math.max(0, baseCupo - totalCantidad);
          resumenCupoEl.textContent = nuevo;

          const barra = document.getElementById('resumen-cupo-barra');
          if (barra) {
            const pct = maximo > 0 ? Math.min(100, (nuevo / maximo) * 100) : 0;
            barra.style.width = pct + '%';
            barra.className = 'h-full rounded-full transition-all duration-300 ' + (
              nuevo <= 0 ? 'bg-rose-500' :
              nuevo <= Math.ceil(maximo / 3) ? 'bg-amber-500' : 'bg-emerald-500'
            );
          }
        }

        
        const detalleCupoEl = document.getElementById('cupo-libre-detalle');
        if (detalleCupoEl) {
          const baseCupo = parseInt(detalleCupoEl.dataset.baseCupo || '0', 10);
          const maximo   = parseInt(detalleCupoEl.dataset.maximo   || '1',  10);
          const nuevo    = Math.max(0, baseCupo - totalCantidad);
          detalleCupoEl.textContent = nuevo;

          const barra = document.getElementById('cupo-libre-detalle-barra');
          if (barra) {
            const pct = maximo > 0 ? Math.min(100, (nuevo / maximo) * 100) : 0;
            barra.style.width = pct + '%';
            barra.style.backgroundColor = nuevo <= 0 ? '#ef4444' :
                                         nuevo <= Math.ceil(maximo / 3) ? '#f59e0b' : '#10b981';
          }
        }

        
        if (ctx === 'catalogo') {
          const libroId = _obtenerLibroIdDesdeUrl(url);
          _reemplazarFormConBotonCarrito(
            form,
            'w-full inline-flex items-center justify-center gap-2 rounded-xl border ' +
              'border-indigo-600/30 bg-indigo-600/10 px-4 py-2 text-xs font-bold text-indigo-700 ' +
              'hover:bg-indigo-600/20 transition-all duration-300 cursor-pointer',
            '<i data-lucide="shopping-cart" class="h-3.5 w-3.5"></i> En tu carrito',
            libroId
          );
        } else if (ctx === 'detalle') {
          const libroId = _obtenerLibroIdDesdeUrl(url);
          _reemplazarFormConBotonCarrito(
            form,
            'shrink-0 rounded-2xl border border-indigo-600/30 bg-indigo-600/10 px-5 py-3 ' +
              'text-sm font-bold text-indigo-700 transition hover:bg-indigo-600/20 cursor-pointer',
            'Ya está en tu carrito',
            libroId
          );
        }

        if (ctx === 'drawer' && form.dataset.action === 'quitar') {
          const libroId = _obtenerLibroIdDesdeUrl(url);
          if (libroId && !_libroExisteEnDrawer(libroId)) {
            _restaurarBotonAgregarCatalogo(libroId);
          }
        }

        if (submitBtn) submitBtn.disabled = false;
      })
      .catch(() => {
        showToast('Error de conexión. Intenta de nuevo.', 'error');
        if (submitBtn) submitBtn.disabled = false;
      });
  });
}


function _reemplazarFormConBotonCarrito(form, className, innerHTML, libroId = null) {
  const parent = form.parentElement;
  if (!parent) return;
  const button     = document.createElement('button');
  button.type      = 'button';
  button.className = className;
  button.innerHTML = innerHTML;
  button.dataset.openCartDrawer = '';
  if (libroId) button.dataset.carritoLinkLibroId = libroId;
  parent.appendChild(button);
  form.remove();
  initIcons();
}

function _obtenerLibroIdDesdeUrl(url) {
  return url.match(/\/libros\/(\d+)/)?.[1] || null;
}

function _libroExisteEnDrawer(libroId) {
  return Boolean(document.querySelector(`#drawer-lista-libros [data-libro-id="${libroId}"]`));
}

function _restaurarBotonAgregarCatalogo(libroId) {
  const card = document.querySelector(`article[data-libro-id="${libroId}"]`);
  const detail = document.querySelector(`[data-detalle-libro-id="${libroId}"]`);
  const container = card || detail;
  const cartButton = container?.querySelector(`[data-carrito-link-libro-id="${libroId}"], [data-open-cart-drawer]`);
  if (!container || !cartButton) return;

  const form = document.createElement('form');
  form.action = `/portal/carrito/libros/${libroId}`;
  form.method = 'post';
  form.className = 'form-carrito-ajax';
  form.dataset.context = card ? 'catalogo' : 'detalle';

  const submitButton = document.createElement('button');
  submitButton.type = 'submit';
  submitButton.className = card
    ? 'w-full inline-flex items-center justify-center gap-2 rounded-xl bg-stone-950 px-4 py-2 text-xs font-bold text-white transition hover:bg-stone-800 cursor-pointer'
    : 'w-full rounded-2xl bg-stone-950 px-5 py-3 text-sm font-bold text-white shadow-sm transition hover:bg-stone-800 sm:w-auto cursor-pointer';
  submitButton.innerHTML = card
    ? '<i data-lucide="shopping-cart" class="h-3.5 w-3.5"></i> Agregar al carrito'
    : 'Agregar al carrito';

  form.appendChild(submitButton);
  cartButton.replaceWith(form);
  initIcons();
}
