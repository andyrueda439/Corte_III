# Control de equipos de laboratorio

Aplicación de escritorio JavaFX que administra el catálogo de equipos de un laboratorio de informática y sus tipos, con persistencia en PostgreSQL mediante **Hibernate ORM** (JPA).

- Asignatura: Programación de Aplicaciones de Escritorio
- Laboratorio evaluado: Corte III
- Repositorio: https://github.com/andyrueda439/Corte_III

## Requisitos
- JDK 21
- Maven (incluido en IntelliJ IDEA mediante `mvnw`)
- PostgreSQL 14 o superior y pgAdmin
- IntelliJ IDEA

Dependencias (en `pom.xml`): JavaFX 21.0.6 (`controls` y `fxml`), Hibernate ORM `hibernate-core` 7.2.25.Final y el driver JDBC `postgresql` 42.7.11.

## Configuración de PostgreSQL
1. Abrir pgAdmin y conectarse a la base `postgres`.
2. Ejecutar el script `sql/script_laboratorio.sql`:
    - **Parte A** (conectado a `postgres`): `CREATE DATABASE bdlaboratorioorm;`
    - **Parte B** (conectado a `bdlaboratorioorm`): crea el esquema `laboratorio`, las tablas, las restricciones y los datos iniciales (3 tipos y 8 equipos con distintos estados).
3. Comprobar con `SELECT * FROM laboratorio.equipo;` que existen 8 equipos.

La estructura se creó manualmente con SQL (no la genera Hibernate). Hibernate está configurado con `hbm2ddl.auto = validate`, es decir, solo valida que las entidades coincidan con las tablas.

## Configuración de la conexión
El archivo `src/main/resources/META-INF/persistence.xml` define la unidad de persistencia **`laboratorioPU`**:
- URL: `jdbc:postgresql://localhost:5432/bdlaboratorioorm`
- Usuario: `postgres` (cambiar si se usa otro)
- **La contraseña no está en el repositorio**: se lee de la variable de entorno `DB_PASSWORD`.

## Cómo iniciar la aplicación
1. Abrir el proyecto en IntelliJ IDEA y hacer *Maven > Reload project*.
2. En *Run > Edit Configurations* crear una configuración **Application** con:
    - Main class: `ni.edu.uam.ejercicioc3.Launcher`
    - Environment variables: `DB_PASSWORD=<contraseña de PostgreSQL>`
3. Ejecutar la configuración **Launcher**.

## Estructura del proyecto
| Paquete | Contenido |
|---|---|
| `model` | Entidades `TipoEquipo` y `Equipo` |
| `controller` | `LaboratorioController` (eventos y componentes gráficos) |
| `dao` | `EquipoDAO` y `TipoEquipoDAO` (consultas y transacciones) |
| `database` | `JPAUtil` (administra la `EntityManagerFactory` y los `EntityManager`) |
| `resources` | `laboratorio-view.fxml` y `META-INF/persistence.xml` |

## Entidades y asociación
- **TipoEquipo** (`laboratorio.tipo_equipo`): `id_tipo`, `nombre` (único) y `descripcion`.
- **Equipo** (`laboratorio.equipo`): `id_equipo`, `codigo` (único), `nombre`, `marca`, `estado`, `cantidad` e `id_tipo`.
- **Asociación uno a muchos**: un tipo tiene varios equipos y cada equipo pertenece a un único tipo.
    - En `Equipo` (lado propietario): `@ManyToOne` con `@JoinColumn(name = "id_tipo")`.
    - En `TipoEquipo` (lado inverso): `@OneToMany(mappedBy = "tipoEquipo")`.
- Restricciones en PostgreSQL: `UNIQUE` en `codigo` y en `tipo_equipo.nombre`, `CHECK (cantidad >= 0)`, `CHECK` de estados permitidos (`Disponible`, `En mantenimiento`, `Fuera de servicio`) y `FOREIGN KEY` hacia `tipo_equipo`.
- **Política de integridad**: sin borrado en cascada. La clave foránea usa `ON DELETE RESTRICT`, así que un tipo con equipos asociados no se puede eliminar.

## Operaciones y consultas
- **Crear**: `persist()` dentro de una transacción (`begin()` / `commit()`).
- **Leer**: JPQL sobre entidades (`select e from Equipo e join fetch e.tipoEquipo ...`).
- **Actualizar**: se verifica la existencia con `find()` y luego `merge()`.
- **Eliminar**: `remove()` dentro de una transacción, con confirmación previa.
- **Consultar**: búsqueda parcial por nombre y filtro por tipo, combinables, con JPQL y `setParameter()` (sin concatenar entradas del usuario).
- **Transacciones**: `commit()` solo al concluir bien; ante una excepción se ejecuta `rollback()`.
- Un `EntityManager` por unidad de trabajo (se cierra en el `finally`); la `EntityManagerFactory` se crea una sola vez y se cierra al salir de la aplicación.

## Validaciones
Código o nombre vacíos, cantidad no numérica o negativa, tipo sin elegir, estado no permitido y código duplicado. Los mensajes de éxito solo se muestran si la operación terminó bien; ante un error se muestra un `Alert` y la ventana sigue abierta.

## Pruebas
La matriz de pruebas P1 a P8 y la demostración de `rollback()` están en [`pruebas.md`](pruebas.md), con las capturas en la carpeta [`evidencias`](evidencias).