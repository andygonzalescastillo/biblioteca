# ==========================================
# Etapa 1: Compilación y Empaquetado (Build)
# ==========================================
FROM eclipse-temurin:21-jdk AS build

# Definir el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el Maven Wrapper y el archivo de configuración POM
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Dar permisos de ejecución al script del Maven Wrapper
RUN chmod +x mvnw

# Descargar las dependencias en una capa separada para aprovechar la caché de Docker
RUN ./mvnw dependency:go-offline

# Copiar el código fuente del backend al contenedor
COPY src ./src

# Compilar y generar el archivo JAR (omitiendo tests y el compilado de Tailwind para agilizar el build)
RUN ./mvnw clean package -DskipTests -DskipTailwind=true

# ==========================================
# Etapa 2: Imagen de Ejecución (Runtime)
# ==========================================
FROM eclipse-temurin:21-jre

# Definir el directorio de trabajo para la ejecución
WORKDIR /app

# Copiar únicamente el archivo JAR empaquetado desde la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto por el cual escuchará la aplicación (ej. Render u otros servidores)
EXPOSE 8080

# Comando por defecto para iniciar la aplicación Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]