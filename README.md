# Sistema de Gestión de Biblioteca 📚

Este es un proyecto integral para la gestión y administración de una biblioteca digital. El sistema cuenta con **dos interfaces principales** para diferentes tipos de usuarios:

1. **Portal del Lector (Web MVC)**: Un portal web público desarrollado en el backend con Spring Boot MVC (Thymeleaf) que permite a los lectores buscar libros, gestionar su carrito y solicitar reservas y préstamos.
2. **Panel de Administración (Frontend SPA)**: Una aplicación interactiva de una sola página (SPA) en React encargada de toda la administración interna del catálogo (gestión de libros, autores, categorías, usuarios/lectores y control de préstamos).

---

## 🔗 Enlaces del Proyecto (Demos en Vivo)

Puedes probar ambas partes del sistema a través de los siguientes enlaces:

* **Panel de Administración (React)**: [https://biblioteca-gestor.vercel.app](https://biblioteca-gestor.vercel.app)
* **Portal del Lector (Spring Boot MVC)**: [https://biblioteca-qkj6.onrender.com/portal/login](https://biblioteca-qkj6.onrender.com/portal/login)

---

## 🏛️ Estructura del Proyecto

El sistema está organizado en un monorrepo que separa claramente la lógica del cliente y el servidor:

```
├── backend/          # Servidor Spring Boot: API REST (para React) y Portal MVC con Thymeleaf (para Lectores)
├── frontend/         # Interfaz administrativa SPA construida con React y TypeScript
└── README.md         # Documentación general
```

---

## 🛠️ Stack Tecnológico

### Backend (`/backend`)
* **Core**: Java 21 & Spring Boot 3.x
* **Acceso a Datos**: Spring Data JPA & Hibernate con Base de Datos PostgreSQL
* **Portal del Lector**: Spring MVC con Thymeleaf (HTML, CSS y JavaScript)
* **API REST**: Controladores REST para alimentar de forma asíncrona la SPA en React
* **Pruebas**: JUnit 5 & Mockito (Más de 360 pruebas automatizadas)
* **Otros integrados**: Docker (Base de datos local), Cloudinary SDK (Almacenamiento de imágenes) y Spring Boot Dotenv

### Frontend (`/frontend`)
* **Core**: React 19 & TypeScript
* **Herramienta de Construcción**: Vite
* **Consumo de APIs**: TanStack Query (React Query) & Axios
* **Diseño e Interfaz**: TailwindCSS (con modo oscuro y componentes dinámicos)
* **Formularios y Validación**: React Hook Form & Zod
* **Enrutamiento**: React Router DOM

---

## 🚀 Cómo Ejecutar en Local

### 1. Servidor Backend
1. Entra a la carpeta del backend:
   ```bash
   cd backend
   ```
2. Levanta la base de datos PostgreSQL local usando Docker:
   ```bash
   docker compose up -d
   ```
3. Configura tus credenciales de Cloudinary en un archivo `.env` en la raíz de `backend/`:
   ```env
   CLOUDINARY_CLOUD_NAME=tu_cloud_name
   CLOUDINARY_API_KEY=tu_api_key
   CLOUDINARY_API_SECRET=tu_api_secret
   ```
4. Compila y ejecuta la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```
   * El Portal del Lector MVC estará disponible en: `http://localhost:8080/portal/login`
   * Los servicios API REST estarán expuestos en: `http://localhost:8080/api`

### 2. Cliente Frontend
1. Entra a la carpeta del frontend:
   ```bash
   cd ../frontend
   ```
2. Instala las dependencias de Node:
   ```bash
   npm install
   ```
3. Corre el servidor de desarrollo:
   ```bash
   npm run dev
   ```
   * El Panel de Administración estará disponible en: `http://localhost:5173`
