export const formatearFecha = (dateStr?: string, options?: Intl.DateTimeFormatOptions): string => {
  if (!dateStr) return 'Sin fecha registrada';
  try {
    const iso = dateStr.includes('T') ? dateStr : `${dateStr}T00:00:00`;
    return new Date(iso).toLocaleDateString('es-ES', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      ...options,
    });
  } catch {
    return dateStr;
  }
};

export const fechaAFormatoISO = (fecha: Date): string => {
  const year = fecha.getFullYear();
  const month = String(fecha.getMonth() + 1).padStart(2, '0');
  const day = String(fecha.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};
