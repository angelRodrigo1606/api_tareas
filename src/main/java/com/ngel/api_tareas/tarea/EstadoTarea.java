package com.ngel.api_tareas.tarea;

import java.util.Arrays;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoTarea {
    PENDIENTE,
    COMPLETADA;

    /** Representación en la API: en minúsculas ("pendiente", "completada"). */
    @JsonValue
    public String valor() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Usado también al leer JSON, para aceptar "pendiente", "PENDIENTE", etc. */
    @JsonCreator
    public static EstadoTarea desde(String valor) {
        return Arrays.stream(values())
                .filter(estado -> estado.name().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de tarea desconocido: " + valor));
    }
}
