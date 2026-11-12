package com.ngel.api_tareas.tarea;

public class TareaNoEncontradaException extends RuntimeException {

    private final Long id;

    public TareaNoEncontradaException(Long id) {
        super("No existe ninguna tarea con id " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
