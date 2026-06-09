
export function initIcons() {
  if (typeof lucide !== 'undefined') {
    lucide.createIcons();
  }
}


window.initIcons = initIcons;
