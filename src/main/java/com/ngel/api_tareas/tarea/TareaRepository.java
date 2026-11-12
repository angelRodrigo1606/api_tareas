package com.ngel.api_tareas.tarea;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Repository;

/** Almacenamiento en memoria; se pierde al reiniciar la aplicación. */
@Repository
public class TareaRepository {

    private final Map<Long, Tarea> tareas = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    /**
     * Inserta una tarea nueva y le asigna un id.
     *
     * @throws IllegalArgumentException si la tarea ya trae id (para modificarla, usa {@link #actualizar})
     */
    public Tarea crear(Tarea tarea) {
        if (tarea.id() != null) {
            throw new IllegalArgumentException("Una tarea nueva no puede traer id: " + tarea.id());
        }
        Tarea creada = tarea.conId(secuencia.incrementAndGet());
        tareas.put(creada.id(), creada);
        return creada;
    }

    /**
     * Reemplaza una tarea existente. La comprobación y el reemplazo son atómicos.
     *
     * @return la tarea actualizada, o vacío si no existe ninguna con ese id
     * @throws IllegalArgumentException si la tarea no trae id
     */
    public Optional<Tarea> actualizar(Tarea tarea) {
        if (tarea.id() == null) {
            throw new IllegalArgumentException("Para actualizar una tarea hace falta su id");
        }
        return Optional.ofNullable(tareas.computeIfPresent(tarea.id(), (id, actual) -> tarea));
    }

    /**
     * Aplica un cambio sobre el estado actual de una tarea existente. Leer, modificar y guardar
     * ocurre de forma atómica, así que no se pierden cambios hechos en paralelo.
     *
     * @return la tarea modificada, o vacío si no existe ninguna con ese id
     * @throws IllegalStateException si el cambio intenta alterar el id
     */
    public Optional<Tarea> actualizar(Long id, UnaryOperator<Tarea> cambio) {
        return Optional.ofNullable(tareas.computeIfPresent(id, (clave, actual) -> {
            Tarea modificada = cambio.apply(actual);
            if (!clave.equals(modificada.id())) {
                throw new IllegalStateException("El cambio no puede modificar el id de la tarea " + clave);
            }
            return modificada;
        }));
    }

    /** @return true si la tarea existía y se ha eliminado */
    public boolean eliminar(Long id) {
        return tareas.remove(id) != null;
    }

    public Optional<Tarea> buscarPorId(Long id) {
        return Optional.ofNullable(tareas.get(id));
    }

    public List<Tarea> buscarTodas() {
        return tareas.values().stream()
                .sorted(Comparator.comparing(Tarea::id))
                .toList();
    }

    public List<Tarea> buscarPorEstado(EstadoTarea estado) {
        return tareas.values().stream()
                .filter(tarea -> tarea.estado() == estado)
                .sorted(Comparator.comparing(Tarea::id))
                .toList();
    }
}
