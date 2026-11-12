package com.ngel.api_tareas.tarea;

public record Tarea(Long id, String titulo, String descripcion, EstadoTarea estado) {

    /** Tarea nueva, aún sin id, en estado pendiente. */
    public static Tarea nueva(String titulo, String descripcion) {
        return new Tarea(null, titulo, descripcion, EstadoTarea.PENDIENTE);
    }

    public Tarea conId(Long nuevoId) {
        return new Tarea(nuevoId, titulo, descripcion, estado);
    }

    public Tarea completar() {
        return new Tarea(id, titulo, descripcion, EstadoTarea.COMPLETADA);
    }
}
