# Microservicio Catálogo — R&&M Detalles

Primer incremento del backend: catálogo público y administración protegida de categorías y productos.

## Tecnologías

- Java 21
- Spring Boot 4.1.2
- Spring MVC, Spring Data JPA y PostgreSQL
- Spring Security Resource Server para JWT de Supabase Auth

## Endpoints iniciales

| Método | Ruta | Acceso | Uso |
|---|---|---|---|
| GET | `/api/v1/catalogo/categorias` | Público | Lista las categorías activas. |
| GET | `/api/v1/catalogo/productos` | Público | Lista los productos activos y publicados. Acepta `?categoriaId=<uuid>`. |
| GET | `/api/v1/catalogo/productos/{id}` | Público | Consulta un producto publicado. |
| GET | `/api/v1/categorias?soloActivas=true` | Administrador | Lista categorías. |
| POST | `/api/v1/categorias` | Administrador | Crea una categoría. |
| PUT | `/api/v1/categorias/{id}` | Administrador | Actualiza una categoría. |
| DELETE | `/api/v1/categorias/{id}` | Administrador | Desactiva una categoría. |
| GET | `/api/v1/productos` | Administrador | Lista productos activos e inactivos para administración. |
| POST | `/api/v1/productos` | Administrador | Crea un producto. |
| PUT | `/api/v1/productos/{id}` | Administrador | Actualiza un producto. |
| DELETE | `/api/v1/productos/{id}` | Administrador | Desactiva un producto. |
| GET | `/actuator/health` | Público | Estado de salud del servicio. |

Las rutas administrativas exigen un JWT válido emitido por Supabase Auth. El `sub` del token se relaciona con `rm_detalles.usuarios.id`; el rol activo de esa tabla determina la autoridad. Las lecturas públicas solo exponen productos publicados y activos.

## Preparar conexión local

1. Instala Java 21 y Maven 3.9 o superior.
2. Usa `.env.example` como guía y obtén los datos desde **Supabase > Connect > Connection string** y la referencia del proyecto.
3. Configura las variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SUPABASE_ISSUER_URI`, `SUPABASE_JWK_SET_URI` y `FRONTEND_ORIGIN` en la configuración de ejecución de IntelliJ IDEA o en el entorno del sistema. Spring Boot no carga automáticamente el archivo `.env`.
4. No compartas ni publiques `DB_PASSWORD`. El backend se conecta directamente a PostgreSQL; el frontend no recibe esta credencial.
5. Ejecuta `mvn spring-boot:run` desde esta carpeta.

`spring.jpa.hibernate.ddl-auto=validate` hace que Hibernate valide el mapeo contra el esquema existente; no crea ni modifica las tablas. El esquema `rm_detalles` debe estar creado mediante el script SQL acordado.

## JWT de Supabase

Los proyectos nuevos usan claves de firma asimétricas por defecto. El servicio usa el issuer del proyecto y su endpoint JWKS para validar la firma y el `iss` del token. No se debe colocar una clave secreta de Supabase en el frontend.

## Alcance de este primer incremento

Incluye endpoints CRUD para categorías y productos, filtros para el catálogo público, validación de entrada, desactivación lógica y autorización por rol. La carga de imágenes a Storage y el mantenimiento de `producto_imagenes` se implementarán como el siguiente incremento, después de confirmar la conexión del servicio.
