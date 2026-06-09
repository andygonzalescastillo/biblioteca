
import { initIcons } from './modules/icons.js';
import { showToast } from './modules/toast.js';
import { initDrawer, initMenuMobile } from './modules/menu.js';
import { initCarritoAjax } from './modules/carrito-ajax.js';
import { initCatalogo } from './modules/catalogo.js';
import { initCarritoPage } from './modules/carrito-page.js';
import { initTheme } from './modules/theme.js';

function initThymeleafToasts() {
  
  if (document.querySelector('[data-error-page]')) return;

  const msgEl = document.getElementById('thymeleaf-mensaje');
  const errEl = document.getElementById('thymeleaf-error');
  if (msgEl) showToast(msgEl.dataset.message, 'success');
  if (errEl) showToast(errEl.dataset.error, 'error');
}


document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  initIcons();
  initThymeleafToasts();
  initCarritoAjax();
  initDrawer();
  initMenuMobile();
  initCatalogo();
  initCarritoPage();
});
