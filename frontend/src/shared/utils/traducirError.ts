export const DICCIONARIO_ERRORES: Record<string, string> = {
  NETWORK_ERROR: 'No se pudo establecer conexión con el servidor. Verifica tu conexión a internet o si el servicio está activo.',
  INTERNAL_SERVER_ERROR: 'Ha ocurrido un error interno en el servidor. Por favor, inténtalo de nuevo más tarde.',
  BAD_REQUEST: 'La solicitud contiene datos no válidos o mal formateados.',
  NOT_FOUND: 'El recurso solicitado no fue encontrado.',
  UNAUTHORIZED: 'No tienes autorización para realizar esta acción. Inicia sesión nuevamente.',
  FORBIDDEN: 'No tienes permisos suficientes para acceder a este recurso.',

  INVALID_LOAN_DAYS: 'La duración del préstamo es inválida.',
  BOOK_INACTIVE: 'Este libro no está disponible para préstamos (inactivo).',
  INSUFFICIENT_STOCK: 'No hay suficiente stock disponible para este libro.',
  USER_INACTIVE: 'La cuenta del usuario está inactiva.',
  USER_HAS_OVERDUE_LOANS: 'El usuario no puede realizar operaciones por tener entregas atrasadas pendientes.',
  BOOK_ALREADY_LOANED_BY_USER: 'El usuario ya posee una copia de este libro actualmente.',
  EMAIL_ALREADY_EXISTS: 'Ya existe un usuario registrado con ese mismo correo electrónico.',
  USER_HAS_ACTIVE_LOANS: 'No se puede desactivar el usuario porque tiene préstamos activos asociados.',
  EMPTY_LOAN_DETAILS: 'El préstamo debe contener al menos un libro.',
  MAX_CONCURRENT_LOANS_EXCEEDED: 'El usuario ha alcanzado el límite máximo de libros prestados simultáneamente.',
  INVALID_QUANTITY: 'La cantidad del préstamo debe ser mayor a 0 o no debe exceder el límite permitido.',
  LOAN_ALREADY_RETURNED: 'Este préstamo ya fue devuelto anteriormente.',
  ISBN_ALREADY_EXISTS: 'Ya existe un libro registrado con ese mismo código ISBN.',
  BOOK_HAS_ACTIVE_LOANS: 'No se puede desactivar el libro porque está asociado a un préstamo activo.',
  EMPTY_FILE: 'No se puede guardar un archivo de imagen vacío.',
  INVALID_IMAGE_TYPE: 'Solo se permiten imágenes en formatos JPG, PNG o WEBP.',
  INVALID_IMAGE_EXTENSION: 'La imagen debe tener extensión válida (JPG, PNG o WEBP).',
  CATEGORY_ALREADY_EXISTS: 'Ya existe una categoría con ese mismo nombre.',
  CATEGORY_HAS_ACTIVE_BOOKS: 'No se puede desactivar la categoría porque tiene libros activos asociados.',
  AUTHOR_HAS_ACTIVE_BOOKS: 'No se puede desactivar el autor porque tiene libros activos asociados.',
};

export const traducirCodigoError = (errorCode: string, mensajePorDefecto: string): string => {
  const codigoLimpio = errorCode.trim().toUpperCase();
  return DICCIONARIO_ERRORES[codigoLimpio] || mensajePorDefecto;
}
