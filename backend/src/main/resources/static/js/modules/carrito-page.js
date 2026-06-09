  
export function initCarritoPage() {
  const input   = document.getElementById('diasPrestamo');
  const modal   = document.getElementById('modal-confirmar-prestamo');
  if (!input || !modal) return; 

  const btnMenos = document.getElementById('btn-dias-menos');
  const btnMas   = document.getElementById('btn-dias-mas');
  const modalDias = document.getElementById('modal-dias-texto');
  const btnAbrir  = document.getElementById('btn-abrir-modal-prestamo');
  const btnConf   = document.getElementById('btn-confirmar-prestamo');
  const btnCanc   = document.getElementById('btn-cancelar-modal-prestamo');
  const form      = document.getElementById('form-prestamo');

  function actualizarFechaDevolucion() {
    const dias = parseInt(input.value, 10);
    if (isNaN(dias)) return;
    const fecha = new Date();
    fecha.setDate(fecha.getDate() + dias);
    const opciones = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    let formatted  = fecha.toLocaleDateString('es-ES', opciones);
    formatted = formatted.charAt(0).toUpperCase() + formatted.slice(1);
    const elFecha = document.getElementById('resumen-fecha-devolucion');
    if (elFecha) elFecha.textContent = formatted;
  }

  input.addEventListener('input', actualizarFechaDevolucion);

  btnMenos?.addEventListener('click', () => {
    const val = parseInt(input.value, 10);
    const min = parseInt(input.min,   10);
    if (val > min) { input.value = val - 1; actualizarFechaDevolucion(); }
  });

  btnMas?.addEventListener('click', () => {
    const val = parseInt(input.value, 10);
    const max = parseInt(input.max,   10);
    if (val < max) { input.value = val + 1; actualizarFechaDevolucion(); }
  });

  
  actualizarFechaDevolucion();

  
  btnAbrir?.addEventListener('click', () => {
    if (modalDias) modalDias.textContent = input.value;
    modal.classList.remove('hidden');
  });

  btnCanc?.addEventListener('click', () => modal.classList.add('hidden'));

  
  modal.addEventListener('click', (e) => {
    if (e.target === modal) modal.classList.add('hidden');
  });

  
  btnConf?.addEventListener('click', () => form?.submit());
}
