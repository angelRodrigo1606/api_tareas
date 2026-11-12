package com.ngel.api_tareas.tarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TareaRepositoryTest {

    private final TareaRepository repository = new TareaRepository();

    @Test
    void crearAsignaIdsNuevos() {
        Tarea primera = repository.crear(Tarea.nueva("Una", null));
        Tarea segunda = repository.crear(Tarea.nueva("Otra", null));

        assertThat(primera.id()).isNotNull();
        assertThat(segunda.id()).isNotEqualTo(primera.id());
        assertThat(repository.buscarTodas()).containsExactly(primera, segunda);
    }

    @Test
    void crearRechazaTareaConId() {
        Tarea existente = repository.crear(Tarea.nueva("Original", null));

        assertThatThrownBy(() -> repository.crear(new Tarea(existente.id(), "Impostora", null, EstadoTarea.PENDIENTE)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(repository.buscarPorId(existente.id())).contains(existente);
    }

    @Test
    void actualizarReemplazaTareaExistente() {
        Tarea original = repository.crear(Tarea.nueva("Leer", null));
        Tarea modificada = new Tarea(original.id(), "Leer", "Capítulo 3", EstadoTarea.COMPLETADA);

        assertThat(repository.actualizar(modificada)).contains(modificada);
        assertThat(repository.buscarPorId(original.id())).contains(modificada);
    }

    @Test
    void actualizarDevuelveVacioSiNoExisteYNoLaCrea() {
        Tarea inexistente = new Tarea(42L, "Fantasma", null, EstadoTarea.PENDIENTE);

        assertThat(repository.actualizar(inexistente)).isEmpty();
        assertThat(repository.buscarPorId(42L)).isEmpty();
        assertThat(repository.buscarTodas()).isEmpty();
    }

    @Test
    void actualizarConCambioPartiendoDelEstadoActual() {
        Tarea original = repository.crear(Tarea.nueva("Leer", "Capítulo 3"));

        assertThat(repository.actualizar(original.id(), Tarea::completar))
                .contains(new Tarea(original.id(), "Leer", "Capítulo 3", EstadoTarea.COMPLETADA));
    }

    @Test
    void actualizarConCambioDevuelveVacioSiNoExiste() {
        assertThat(repository.actualizar(42L, Tarea::completar)).isEmpty();
        assertThat(repository.buscarTodas()).isEmpty();
    }

    @Test
    void actualizarConCambioNoPermiteCambiarElId() {
        Tarea original = repository.crear(Tarea.nueva("Leer", null));

        assertThatThrownBy(() -> repository.actualizar(original.id(), t -> t.conId(99L)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(repository.buscarPorId(original.id())).contains(original);
        assertThat(repository.buscarPorId(99L)).isEmpty();
    }

    @Test
    void eliminarBorraLaTareaExistente() {
        Tarea tarea = repository.crear(Tarea.nueva("Borrar", null));

        assertThat(repository.eliminar(tarea.id())).isTrue();
        assertThat(repository.buscarPorId(tarea.id())).isEmpty();
    }

    @Test
    void eliminarDevuelveFalseSiNoExiste() {
        assertThat(repository.eliminar(42L)).isFalse();
    }

    @Test
    void noReutilizaIdsDeTareasEliminadas() {
        Tarea borrada = repository.crear(Tarea.nueva("Borrar", null));
        repository.eliminar(borrada.id());

        Tarea nueva = repository.crear(Tarea.nueva("Nueva", null));

        assertThat(nueva.id()).isNotEqualTo(borrada.id());
    }

    @Test
    void actualizarRechazaTareaSinId() {
        assertThatThrownBy(() -> repository.actualizar(Tarea.nueva("Sin id", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
