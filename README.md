# Realización de Obras Públicas - API

Este repositorio contiene el backend (API REST) para el **Sistema Empresarial de Gestión de Obras Públicas**, diseñado para administrar la planificación, ejecución, documentación y control geográfico de proyectos de infraestructura pública.

El sistema está construido con **Spring Boot 3**, **PostgreSQL**, y cuenta con un sistema robusto de seguridad basado en roles y permisos mediante **JWT (JSON Web Tokens)**.

---

## 🔗 Enlaces del Repositorio

*   **HTTPS:** `https://github.com/oaxacatlaxiaco50-alt/Realizacion_Obras.git`
*   **SSH:** `git@github.com:oaxacatlaxiaco50-alt/Realizacion_Obras.git`

---

## 🛠️ Stack Tecnológico

| Tecnología / Librería | Versión | Descripción |
| :--- | :--- | :--- |
| **Java** | 21 | Versión de Java utilizada (Temurin JDK) |
| **Spring Boot** | 3.4.1 | Framework principal para desarrollo de la API |
| **PostgreSQL** | 16 | Motor de base de datos relacional |
| **Liquibase** | Core | Gestión y control de versiones del esquema de base de datos |
| **Spring Security** | - | Módulo de seguridad y autorización del sistema |
| **JJWT (Java JWT)** | 0.12.6 | Creación, firma y verificación de tokens de autenticación JWT |
| **MapStruct** | 1.6.3 | Mapeo eficiente de Entidades a DTOs |
| **Lombok** | - | Reducción de código boilerplate mediante anotaciones |
| **Springdoc OpenAPI (Swagger)** | 2.8.3 | Documentación interactiva e interactuable de los endpoints |
| **Testcontainers** | - | Pruebas de integración con bases de datos reales en contenedores Docker |

---

## 📁 Estructura del Proyecto

El proyecto está diseñado bajo una arquitectura modular y limpia, organizada en torno a funcionalidades (`features`) y componentes compartidos (`shared`):

```text
src/main/java/com/obraspublicas/
│
├── features/                           # Módulos y funcionalidades de negocio
│   ├── archivos/                      # 📁 Gestión de carpetas y archivos por obra (Legal, Social, etc.)
│   ├── audit/                         # Gestión de bitácoras y registros de auditoría
│   ├── auth/                          # Proceso de inicio de sesión y emisión de JWT
│   ├── avances/                       # 📊 Registro de avances, línea de tiempo y evidencias (fotos/videos)
│   ├── expedientes/                   # Organización de carpetas y documentos de obras
│   ├── geocercas/                     # Delimitación geográfica de perímetros de trabajo
│   ├── obras/                         # CRUD, flujo de vida de obras públicas y 📍 geolocalización
│   ├── permissions/                   # Definición de permisos atómicos
│   ├── roles/                         # Roles de usuario (Admin, Supervisor, etc.)
│   ├── rutas/                         # Registro de trazos de ruta para obras viales
│   └── users/                         # Administración de cuentas de usuario
│
└── shared/                             # Componentes transversales compartidos
    ├── audit/                         # Clases y anotaciones base para auditoría
    ├── config/                        # Configuraciones de beans, CORS y Jackson
    ├── exception/                     # Manejo global de excepciones (GlobalExceptionHandler)
    ├── security/                      # Configuración de filtros JWT y Spring Security
    └── storage/                       # Configuración y servicios de almacenamiento de archivos
```

---

## 🔐 Seguridad: Roles y Permisos

La API implementa un modelo de autorización basado en **RBAC (Role-Based Access Control)** con granularidad a nivel de **Permisos**.

### Roles Disponibles
1.  **ADMINISTRADOR:** Acceso total y sin restricciones a todos los recursos.
2.  **SUPERVISOR:** Administra obras, edita bitácoras y cambia estados de las obras.
3.  **CONTRATISTA:** Visualiza obras asignadas y gestiona los documentos de su expediente.
4.  **AUDITOR:** Acceso exclusivo de lectura para auditorías de logs y bitácoras del sistema.

### Cuentas de Prueba Sembradas (Seeded Users)
Para facilitar las pruebas de desarrollo, se han preconfigurado las siguientes cuentas por medio de migraciones automáticas de Liquibase:

> [!NOTE]
> La contraseña para **todas** las cuentas preestablecidas de prueba es: **`admin123`**

| Usuario (Username) | Correo Electrónico | Rol Asignado | Propósito de Prueba |
| :--- | :--- | :--- | :--- |
| **`jorge`** | `jorge@obraspublicas.com` | `ADMINISTRADOR` | Acceso global a todas las rutas |
| **`supervisor`** | `pedro@obraspublicas.com` | `SUPERVISOR` | Edición de obras y estados |
| **`contratista`** | `juan@obraspublicas.com` | `CONTRATISTA` | Gestión de expedientes específicos |
| **`auditor`** | `ana@obraspublicas.com` | `AUDITOR` | Revisión de logs y auditorías |

---

## 🚀 Guía de Instalación y Ejecución

### Requisitos Previos
*   Java Development Kit (JDK) 21 instalado.
*   Apache Maven 3.8+ o superior.
*   Docker y Docker Compose (opcional, pero recomendado).

---

### Opción A: Ejecución con Docker Compose (Recomendada)
Para levantar todo el ecosistema (PostgreSQL + pgAdmin + Backend API) con un solo comando, ejecuta desde la raíz del proyecto:

```bash
docker compose up --build
```

Esto desplegará los siguientes servicios:
*   **API Backend:** `http://localhost:8080`
*   **Base de datos PostgreSQL:** `localhost:5432` (Base de datos: `obras_db`, Usuario: `postgres`, Contraseña: `postgres`)
*   **pgAdmin 4 (Administrador de DB):** `http://localhost:5050`
    *   *Usuario:* `admin@obraspublicas.com`
    *   *Contraseña:* `admin123`

---

### Opción B: Ejecución Local en Desarrollo
Si prefieres correr la base de datos en un contenedor u localmente, y la aplicación directamente en tu máquina:

1.  **Iniciar la Base de Datos:**
    Asegúrate de tener una base de datos PostgreSQL corriendo en el puerto `5432` llamada `obras_db`.
2.  **Configurar credenciales locales:**
    Ajusta el archivo [application-dev.yml](file:///c:/Users/andre/Obras_Tlaxiaco/Realizacion_Obras/src/main/resources/application-dev.yml) con el usuario y la contraseña correctos de tu PostgreSQL local. Por ejemplo:
    ```yaml
    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/obras_db
        username: postgres
        password: tu_contraseña_aqui
    ```
3.  **Compilar y Ejecutar:**
    Compila el proyecto y ponlo en marcha usando Maven:
    ```bash
    mvn clean package -DskipTests
    mvn spring-boot:run
    ```

En modo desarrollo local, la API correrá por defecto en el puerto **`8081`** (configurado en `application.yml`).

---

## 📖 Documentación de la API (Swagger UI)

Una vez que la aplicación esté ejecutándose, puedes explorar interactiva y visualmente todos los endpoints disponibles de la API:

*   **URL de Swagger UI:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) *(si corre de forma local)*
*   **URL de Swagger UI (Docker):** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) *(si corre mediante Docker)*

---

## 📡 Resumen de Endpoints Principales

| Módulo | Endpoint Base | Operación Clave | Permiso Requerido |
| :--- | :--- | :--- | :--- |
| **Autenticación** | `/auth` | `POST /login` | Público |
| **Obras** | `/obras` | `POST /` (Crear obra) | `OBRA_CREATE` |
| | | `GET /{id}` (Consultar) | `OBRA_VIEW` |
| | | `PATCH /{id}/estatus` | `OBRA_CHANGE_STATUS` |
| | | `GET /mapa` (🗺️ Obtener GeoJSON de obras) | `OBRA_VIEW` |
| **Carpetas/Archivos**| `/obras/{id}/archivos` | `POST /` (Subir archivos) | `OBRA_UPDATE` |
| | | `GET /` (Listar archivos por carpeta) | `OBRA_VIEW` |
| **Avances** | `/obras/{id}/avances` | `POST /` (Crear reporte de avance) | `OBRA_UPDATE` |
| | | `POST /{id}/evidencias` (Subir foto/video) | `OBRA_UPDATE` |
| | | `GET /` (Línea de tiempo de avances) | `OBRA_VIEW` |
| **Expedientes** | `/obras/{obraId}/expediente`| `GET /` (Ver catálogo) | `OBRA_VIEW` |
| **Geocercas** | `/geocercas` | `POST /` (Crear geocerca) | `GEOCERCA_CREATE` |
| **Rutas** | `/rutas` | `POST /` (Crear ruta) | `RUTA_CREATE` |
| **Bitácoras** | `/bitacoras` | `POST /` (Nueva entrada) | `BITACORA_CREATE` |
| **Auditoría** | `/audit-logs` | `GET /` (Consultar logs) | `AUDIT_VIEW` |
| **Usuarios** | `/users` | `GET /` (Lista usuarios) | `USER_VIEW` |

---

## 🗄️ Base de Datos y Migraciones (Liquibase)

El esquema de la base de datos se genera y actualiza automáticamente mediante **Liquibase** al iniciar la aplicación. Las definiciones y el historial de cambios se encuentran en:

*   **Changelog Maestro:** [master.xml](file:///c:/Users/andre/Obras_Tlaxiaco/Realizacion_Obras/src/main/resources/db/changelog/master.xml)
*   **Scripts de Cambios:** Directorio [changes/](file:///c:/Users/andre/Obras_Tlaxiaco/Realizacion_Obras/src/main/resources/db/changelog/changes/)

---

## 🧪 Pruebas Unitarias y de Integración

El proyecto hace uso de **Testcontainers** para instanciar dinámicamente un contenedor ligero de PostgreSQL en Docker durante la fase de testing, garantizando un entorno idéntico al de producción.

Para ejecutar los tests, corre:
```bash
mvn test
```
