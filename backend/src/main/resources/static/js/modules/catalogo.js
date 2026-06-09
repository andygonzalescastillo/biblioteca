
import { initIcons } from './icons.js';

export function initCatalogo() {
  const form           = document.getElementById('form-busqueda');
  if (!form) return; 

  const inputBuscar    = document.getElementById('input-buscar');
  const btnLimpiar     = document.getElementById('btn-limpiar');
  const selectCategoria = document.getElementById('select-categoria');
  const selectAutor    = document.getElementById('select-autor');
  const inputSize      = document.getElementById('input-size');

  let buscando       = false;
  let debounceTimer  = null;

  
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('.btn-size-toggle');
    if (!btn) return;

    const nuevoSize = btn.dataset.size;
    inputSize.value = nuevoSize;

    document.querySelectorAll('.btn-size-toggle').forEach((b) => {
      b.classList.remove('bg-stone-950', 'text-white', 'font-bold', 'shadow-sm');
      b.classList.add('text-stone-500', 'hover:text-stone-950');
    });
    btn.classList.remove('text-stone-500', 'hover:text-stone-950');
    btn.classList.add('bg-stone-950', 'text-white', 'font-bold', 'shadow-sm');

    ajaxBuscar();
  });

  
  inputBuscar?.addEventListener('input', () => {
    btnLimpiar?.classList.toggle('hidden', !inputBuscar.value);
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(ajaxBuscar, 400);
  });

  
  btnLimpiar?.addEventListener('click', () => {
    if (inputBuscar) inputBuscar.value = '';
    btnLimpiar.classList.add('hidden');
    if (selectCategoria) selectCategoria.value = '';
    if (selectAutor) selectAutor.value = '';
    ajaxBuscar();
  });

  
  selectCategoria?.addEventListener('change', ajaxBuscar);
  selectAutor?.addEventListener('change', ajaxBuscar);

  async function ajaxBuscar() {
    if (buscando) return;
    buscando = true;

    const url = new URL(form.action || window.location.href, window.location.origin);
    if (inputBuscar?.value)    url.searchParams.set('buscar',      inputBuscar.value);
    if (selectCategoria?.value) url.searchParams.set('categoriaId', selectCategoria.value);
    if (selectAutor?.value)    url.searchParams.set('autorId',     selectAutor.value);
    url.searchParams.set('size', inputSize?.value || '8');

    try {
      const resp = await fetch(url.toString());
      const html = await resp.text();
      const tmp  = document.createElement('div');
      tmp.innerHTML = html;

      ['resultados-info', 'resultados-container', 'resultados-pagination'].forEach((id) => {
        const nuevo  = tmp.querySelector('#' + id);
        const actual = document.getElementById(id);
        if (id === 'resultados-pagination' && nuevo && actual) {
          actual.replaceWith(nuevo);
        } else if (nuevo && actual) {
          actual.innerHTML = nuevo.innerHTML;
        } else if (!nuevo && actual) {
          actual.innerHTML = '';
        }
      });

      initIcons();
      history.replaceState({}, '', url.toString());
    } catch (err) {
      console.error('Error buscando:', err);
    } finally {
      buscando = false;
    }
  }
}
