package com.ngel.api_tareas.tarea;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/tareas")
public class TareaController {

    private final TareaRepository repository;

    public TareaController(TareaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Tarea> listar(@RequestParam(required = false) EstadoTarea estado) {
        return estado == null ? repository.buscarTodas() : repository.buscarPorEstado(estado);
    }

    @GetMapping("/{id}")
    public Tarea obtener(@PathVariable Long id) {
        return repository.buscarPorId(id).orElseThrow(() -> new TareaNoEncontradaException(id));
    }

    @PostMapping
    public ResponseEntity<Tarea> crear(@Valid @RequestBody CrearTareaRequest request) {
        Tarea creada = repository.crear(Tarea.nueva(request.titulo(), request.descripcion()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(location).body(creada);
    }

    @PutMapping("/{id}")
    public Tarea reemplazar(@PathVariable Long id, @Valid @RequestBody ReemplazarTareaRequest request) {
        Tarea reemplazo = new Tarea(id, request.titulo(), request.descripcion(), request.estado());
        return repository.actualizar(reemplazo).orElseThrow(() -> new TareaNoEncontradaException(id));
    }

    @PatchMapping("/{id}/completar")
    public Tarea completar(@PathVariable Long id) {
        return repository.actualizar(id, Tarea::completar).orElseThrow(() -> new TareaNoEncontradaException(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.eliminar(id)) {
            throw new TareaNoEncontradaException(id);
        }
        return ResponseEntity.noContent().build();
    }
}
