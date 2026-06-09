-- 1. Crear tabla de imágenes
CREATE TABLE imagen (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre_archivo VARCHAR(255) NOT NULL,
    url_almacenamiento VARCHAR(512) NOT NULL,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Crear tabla de categorías
CREATE TABLE categoria (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    estado BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. Crear tabla de autores
CREATE TABLE autor (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    biografia TEXT,
    fecha_nacimiento DATE,
    foto_imagen_id UUID,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_autor_imagen FOREIGN KEY (foto_imagen_id) REFERENCES imagen(id) ON DELETE SET NULL
);


-- 4. Crear tabla de libros
CREATE TABLE libro (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0 CONSTRAINT chk_stock_positivo CHECK (stock >= 0),
    categoria_id BIGINT NOT NULL,
    portada_imagen_id UUID,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_libro_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON DELETE RESTRICT,
    CONSTRAINT fk_libro_imagen FOREIGN KEY (portada_imagen_id) REFERENCES imagen(id) ON DELETE SET NULL
);

-- 5. Crear tabla intermedia para autores y libros (Many-to-Many)
CREATE TABLE libro_autor (
    libro_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL,
    PRIMARY KEY (libro_id, autor_id),
    CONSTRAINT fk_libro_autor_libro FOREIGN KEY (libro_id) REFERENCES libro(id) ON DELETE CASCADE,
    CONSTRAINT fk_libro_autor_autor FOREIGN KEY (autor_id) REFERENCES autor(id) ON DELETE CASCADE
);

-- 6. Crear tabla de usuarios
CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(255),
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    foto_imagen_id UUID,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_usuario_imagen FOREIGN KEY (foto_imagen_id) REFERENCES imagen(id) ON DELETE SET NULL
);

-- 7. Crear tabla de préstamos
CREATE TABLE prestamo (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    fecha_prestamo TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_devolucion_limite TIMESTAMPTZ NOT NULL,
    fecha_devolucion_real TIMESTAMPTZ,
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVO' CONSTRAINT chk_estado_prestamo CHECK (estado IN ('ACTIVO', 'DEVUELTO', 'ATRASADO')),
    CONSTRAINT fk_prestamo_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE RESTRICT
);

-- 8. Crear tabla de detalles de préstamo
CREATE TABLE detalle_prestamo (
    id BIGSERIAL PRIMARY KEY,
    prestamo_id BIGINT NOT NULL,
    libro_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL DEFAULT 1 CONSTRAINT chk_cantidad_positiva CHECK (cantidad > 0),
    CONSTRAINT fk_detalle_prestamo_prestamo FOREIGN KEY (prestamo_id) REFERENCES prestamo(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_prestamo_libro FOREIGN KEY (libro_id) REFERENCES libro(id) ON DELETE RESTRICT
);

-- Crear índices para optimizar consultas frecuentes
CREATE INDEX idx_libro_categoria ON libro(categoria_id);
CREATE INDEX idx_prestamo_usuario ON prestamo(usuario_id);
CREATE INDEX idx_detalle_prestamo_prestamo ON detalle_prestamo(prestamo_id);

-- Crear índices únicos parciales para permitir re-registrar si fue eliminado lógicamente (estado = FALSE)
CREATE UNIQUE INDEX idx_categoria_nombre_active ON categoria(nombre) WHERE (estado = TRUE);
CREATE UNIQUE INDEX idx_libro_isbn_active ON libro(isbn) WHERE (estado = TRUE);
CREATE UNIQUE INDEX idx_usuario_email_active ON usuario(email) WHERE (estado = TRUE);
