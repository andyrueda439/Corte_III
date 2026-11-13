# Matriz de pruebas

Base de datos: `bd_laboratorio_orm` (esquema `laboratorio`). Capturas en la carpeta `evidencias/`.

| ID | Prueba | Entrada | Resultado esperado | Resultado obtenido | Evidencia                          |
|----|--------|---------|--------------------|--------------------|------------------------------------|
| P1 | Registrar un equipo válido | Código `EQ-009`, nombre `Laptop Aspire 5`, marca `Acer`, cantidad `6`, tipo `Computadoras`, estado `Disponible` | Registro guardado y visible | Se mostró "Equipo guardado correctamente" y la fila apareció en la tabla y en PostgreSQL | `P1.png` |
| P2 | Intentar código duplicado | Código `EQ-001` (ya existente) | Operación rechazada; sin duplicados | Alert de advertencia "Ya existe un equipo con el código EQ-001"; no se insertó nada | `P2.png`, `P2.1.png`               |
| P3 | Editar nombre, estado o tipo | `EQ-009`: nombre `Notebook Aspire 5 revisado`, estado `En mantenimiento`, tipo `Redes` | Cambios persistidos | Se mostró "Equipo actualizado correctamente"; los cambios se ven en la tabla y en PostgreSQL | `P3.png`                           |
| P4 | Eliminar un equipo con confirmación | Equipo de prueba `EQ/009` (id 18): Eliminar y confirmar | El registro desaparece del listado y de PostgreSQL | Se pidió confirmación; el registro se eliminó de la tabla y de la base (0 filas) | `P4.png`, `P4.1.png`               |
| P5 | Buscar por nombre parcial | Nombre: `lap` | Solo se muestran coincidencias | Se mostraron únicamente `Laptop Latitude 5420` y `Laptop ThinkPad E14` | `P5.png`                           |
| P6 | Filtrar por tipo y combinar con la búsqueda | Tipo `Computadoras`; luego tipo `Computadoras` + nombre `lap` | Resultados que cumplen ambas condiciones | Solo tipo: 3 equipos; combinado: 2 laptops | `P6.png`, `P6.1.png`               |
| P7 | Ingresar cantidad negativa | Cantidad `-5` (y también `abc`) | Validación rechaza el dato | Alert "La cantidad no puede ser negativa" y "La cantidad debe ser un número entero"; no se guardó nada | `P7.png`, `P7.1.png`               |
| P8 | Reiniciar aplicación | Cerrar y volver a ejecutar | Persisten los datos previamente confirmados | Al reabrir aparecen los 8 equipos originales y `EQ-009` con sus cambios; `EQ/009` no aparece | `P8.png`                           |

## Demostración de rollback()
Clase `PruebaRollback`: en una misma transacción se hace `persist()` de un equipo válido (`EQ-RB1`) y luego `persist()` de otro con código repetido (`EQ-001`).

- **Resultado esperado:** PostgreSQL rechaza el segundo por la restricción `UNIQUE (codigo)`, se ejecuta `rollback()` y no queda ningún cambio parcial.
- **Resultado obtenido:** salió el error `duplicate key value violates unique constraint "uq_equipo_codigo"`, se ejecutó `rollback()`, la cantidad de equipos antes y después es la misma y `EQ-RB1` no existe en la base (0 filas).
- **Evidencia:** `R1.png`, `R2.png`.