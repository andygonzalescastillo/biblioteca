
export function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className =
    'flex items-start gap-3 rounded-2xl border border-stone-200 bg-white ' +
    'p-4 shadow-lg shadow-stone-200/70 pointer-events-auto ' +
    'transition-all duration-300 transform translate-y-4 opacity-0 max-w-sm w-full';

  let colorClass = 'text-indigo-600';
  let iconName   = 'info';

  if (type === 'success' || type === 'mensaje') {
    colorClass = 'text-emerald-600';
    iconName   = 'check-circle';
  } else if (type === 'error') {
    colorClass = 'text-rose-600';
    iconName   = 'alert-triangle';
  }

  const iconWrap = document.createElement('div');
  iconWrap.className = `shrink-0 ${colorClass} flex items-center justify-center`;
  const icon = document.createElement('i');
  icon.dataset.lucide = iconName;
  icon.className = 'h-5 w-5';
  iconWrap.appendChild(icon);

  const content = document.createElement('div');
  content.className = 'flex-1 min-w-0';
  const text = document.createElement('p');
  text.className = 'text-xs font-semibold text-stone-950 leading-relaxed break-words';
  text.textContent = message || '';
  content.appendChild(text);

  const closeButton = document.createElement('button');
  closeButton.type = 'button';
  closeButton.className = 'shrink-0 text-stone-500 hover:text-stone-950 transition duration-200 cursor-pointer flex items-center justify-center';
  closeButton.setAttribute('aria-label', 'Cerrar notificación');
  closeButton.addEventListener('click', () => toast.remove());
  const closeIcon = document.createElement('i');
  closeIcon.dataset.lucide = 'x';
  closeIcon.className = 'h-4 w-4';
  closeButton.appendChild(closeIcon);

  toast.append(iconWrap, content, closeButton);

  container.appendChild(toast);

  if (typeof lucide !== 'undefined') {
    lucide.createIcons();
  }

  
  requestAnimationFrame(() => {
    toast.classList.remove('translate-y-4', 'opacity-0');
  });

  
  setTimeout(() => {
    toast.classList.add('opacity-0', 'scale-95');
    toast.style.transform = 'translateY(-10px)';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}


window.showToast = showToast;
