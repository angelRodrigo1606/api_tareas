# API de Tareas

API REST para gestionar tareas, construida con **Spring Boot 4**. Permite crear, consultar, filtrar, reemplazar, completar y eliminar tareas, con validación de entrada y errores en formato estándar [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) (`application/problem+json`).

> **Nota:** las tareas se guardan en memoria y se pierden al reiniciar la aplicación.

## Tecnologías

- Java 25
- Spring Boot 4.1 (Spring MVC, Bean Validation)
- Maven (incluye Maven Wrapper, no hace falta instalarlo)
- JUnit 5, MockMvc y AssertJ para los tests
- [Bruno](https://www.usebruno.com/) para las pruebas de extremo a extremo

## Puesta en marcha

Requisitos: **JDK 25** o superior.

```bash
git clone https://github.com/angelRodrigo1606/api_tareas.git
cd api_tareas
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Para usar otro puerto:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

En Windows, usa `mvnw.cmd` en lugar de `./mvnw`.

## Modelo

```json
{
  "id": 1,
  "titulo": "Comprar pan",
  "descripcion": "Integral",
  "estado": "pendiente"
}
```

| Campo         | Tipo   | Descripción                                                          |
|---------------|--------|----------------------------------------------------------------------|
| `id`          | número | Generado por el servidor. No se reutiliza aunque se borre la tarea.  |
| `titulo`      | texto  | Obligatorio, no puede estar en blanco. Máximo 200 caracteres.        |
| `descripcion` | texto  | Opcional (`null` si no se envía). Máximo 2000 caracteres.            |
| `estado`      | texto  | `pendiente` o `completada`. Las tareas nuevas empiezan `pendiente`.  |

El estado se acepta sin distinguir mayúsculas (`pendiente`, `PENDIENTE`…) y siempre se devuelve en minúsculas.

## Endpoints

| Método   | Ruta                       | Descripción                                    | Éxito |
|----------|----------------------------|------------------------------------------------|-------|
| `POST`   | `/tareas`                  | Crea una tarea                                 | `201` |
| `GET`    | `/tareas`                  | Lista todas las tareas, ordenadas por `id`     | `200` |
| `GET`    | `/tareas?estado={estado}`  | Lista las tareas con ese estado                | `200` |
| `GET`    | `/tareas/{id}`             | Devuelve una tarea                             | `200` |
| `PUT`    | `/tareas/{id}`             | Reemplaza la tarea completa                    | `200` |
| `PATCH`  | `/tareas/{id}/completar`   | Marca la tarea como completada                 | `200` |
| `DELETE` | `/tareas/{id}`             | Elimina la tarea                               | `204` |

### Crear una tarea

```bash
curl -i -X POST http://localhost:8080/tareas \
  -H "Content-Type: application/json" \
  -d '{"titulo": "Comprar pan", "descripcion": "Integral"}'
```

```http
HTTP/1.1 201 Created
Location: http://localhost:8080/tareas/1
Content-Type: application/json

{"id":1,"titulo":"Comprar pan","descripcion":"Integral","estado":"pendiente"}
```

Solo se leen `titulo` y `descripcion`; cualquier `id` o `estado` enviado se ignora.

### Listar y filtrar

```bash
curl http://localhost:8080/tareas
curl "http://localhost:8080/tareas?estado=pendiente"
curl "http://localhost:8080/tareas?estado=completada"
```

### Consultar una tarea

```bash
curl http://localhost:8080/tareas/1
```

### Reemplazar una tarea

`PUT` sustituye la tarea completa: `titulo` y `estado` son obligatorios, y lo que no se envía queda vacío (por ejemplo, omitir `descripcion` la borra). El `id` siempre es el de la URL. Si la tarea no existe responde `404`; no la crea.

```bash
curl -X PUT http://localhost:8080/tareas/1 \
  -H "Content-Type: application/json" \
  -d '{"titulo": "Comprar pan", "descripcion": "De centeno", "estado": "completada"}'
```

### Completar una tarea

No lleva cuerpo y no modifica el resto de campos. Es idempotente: completar una tarea ya completada vuelve a responder `200`.

```bash
curl -X PATCH http://localhost:8080/tareas/1/completar
```

### Eliminar una tarea

Responde `204 No Content` sin cuerpo.

```bash
curl -i -X DELETE http://localhost:8080/tareas/1
```

## Errores

| Código | Cuándo                                                                                   |
|--------|------------------------------------------------------------------------------------------|
| `400`  | Título vacío, ausente o demasiado largo; estado ausente (en `PUT`) o desconocido; JSON mal formado; `id` no numérico. |
| `404`  | No existe ninguna tarea con ese `id` (en `GET`, `PUT`, `PATCH` y `DELETE`).              |

Los `404` devuelven un cuerpo `application/problem+json`:

```http
HTTP/1.1 404 Not Found
Content-Type: application/problem+json

{
  "title": "Tarea no encontrada",
  "status": 404,
  "detail": "No existe ninguna tarea con id 99",
  "instance": "/tareas/99",
  "id": 99
}
```

## Tests

### Tests de Java

```bash
./mvnw test
```

Incluyen tests unitarios del repositorio y tests de la capa web con MockMvc para todos los endpoints, casos de error incluidos.

### Colección Bruno

La carpeta [`api-tareas-bruno/`](api-tareas-bruno/) contiene una colección de [Bruno](https://www.usebruno.com/) con pruebas de extremo a extremo de todos los endpoints. Cada carpeta crea sus propios datos, así que se puede ejecutar entera o por partes sin reiniciar la API.

Con la aplicación arrancada:

- **Desde la app de Bruno:** abre la carpeta `api-tareas-bruno` como colección y usa *Run* sobre la colección.
- **Desde la terminal:**

  ```bash
  cd api-tareas-bruno
  npx @usebruno/cli run --env local
  ```

La URL base (`baseUrl`) vale `http://localhost:8080` por defecto. Para apuntar a otro servidor, cambia su valor en `environments/local.yml` y selecciona el entorno `local`.

## Estructura del proyecto

El código se organiza por funcionalidad: todo lo relativo a las tareas está en el paquete `tarea`.

```
src/main/java/com/ngel/api_tareas/
├── ApiTareasApplication.java
└── tarea/
    ├── TareaController.java          # Endpoints REST
    ├── Tarea.java                    # Modelo
    ├── EstadoTarea.java              # Estados: pendiente, completada
    ├── CrearTareaRequest.java        # Cuerpo de POST, con validación
    ├── ReemplazarTareaRequest.java   # Cuerpo de PUT, con validación
    ├── TareaRepository.java          # Almacenamiento en memoria
    ├── TareaNoEncontradaException.java
    ├── TareaExceptionHandler.java    # Convierte errores en problem+json
    └── EstadoTareaConverter.java     # ?estado= sin distinguir mayúsculas

src/test/java/com/ngel/api_tareas/tarea/
├── TareaControllerTest.java
└── TareaRepositoryTest.java

api-tareas-bruno/                     # Colección Bruno
```
