import { initIcons } from './icons.js';

const STORAGE_KEY = 'portal-theme';

function getCurrentTheme() {
  return document.documentElement.classList.contains('dark') ? 'dark' : 'light';
}

function applyTheme(theme) {
  const isDark = theme === 'dark';
  document.documentElement.classList.toggle('dark', isDark);
  document.documentElement.style.colorScheme = isDark ? 'dark' : 'light';
  localStorage.setItem(STORAGE_KEY, theme);

  document.querySelectorAll('[data-theme-toggle]').forEach((button) => {
    button.setAttribute('aria-pressed', String(isDark));

    const icon = button.querySelector('[data-theme-icon]');
    const label = button.querySelector('[data-theme-label]');

    if (icon) icon.dataset.lucide = isDark ? 'sun' : 'moon';
    if (label) label.textContent = isDark ? 'Claro' : 'Oscuro';
  });

  initIcons();
}

export function initTheme() {
  document.querySelectorAll('[data-theme-toggle]').forEach((button) => {
    button.addEventListener('click', () => {
      applyTheme(getCurrentTheme() === 'dark' ? 'light' : 'dark');
    });
  });

  applyTheme(getCurrentTheme());
}
