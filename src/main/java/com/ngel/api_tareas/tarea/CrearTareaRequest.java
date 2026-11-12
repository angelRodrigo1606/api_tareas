package com.ngel.api_tareas.tarea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearTareaRequest(
        @NotBlank @Size(max = 200) String titulo,
        @Size(max = 2000) String descripcion) {
}
