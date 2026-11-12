package com.ngel.api_tareas.tarea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Representación completa de una tarea para PUT: lo que no se envía queda vacío. */
public record ReemplazarTareaRequest(
        @NotBlank @Size(max = 200) String titulo,
        @Size(max = 2000) String descripcion,
        @NotNull EstadoTarea estado) {
}
