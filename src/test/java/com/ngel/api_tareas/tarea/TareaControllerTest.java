package com.ngel.api_tareas.tarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import com.jayway.jsonpath.JsonPath;

@WebMvcTest(TareaController.class)
@Import(TareaRepository.class)
class TareaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TareaRepository repository;

    // --- POST /tareas ---

    @Test
    void creaTareaConDescripcion() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "Comprar pan", "descripcion": "Integral"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Comprar pan"))
                .andExpect(jsonPath("$.descripcion").value("Integral"))
                .andExpect(jsonPath("$.estado").value("pendiente"))
                .andReturn();

        Number id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
        assertThat(resultado.getResponse().getHeader("Location"))
                .isEqualTo("http://localhost/tareas/" + id);
    }

    @Test
    void creaTareaSinDescripcion() throws Exception {
        mockMvc.perform(post("/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "Llamar al banco"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.descripcion").value(nullValue()));
    }

    @Test
    void rechazaTituloVacio() throws Exception {
        mockMvc.perform(post("/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "  "}
                                """))
                .andExpect(status().isBadRequest());
    }

    // --- GET /tareas ---

    @Test
    void listaTodasLasTareas() throws Exception {
        Tarea pendiente = repository.crear(Tarea.nueva("Pendiente", null));
        Tarea completada = repository.crear(completada("Completada"));

        mockMvc.perform(get("/tareas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(pendiente.id().intValue())))
                .andExpect(jsonPath("$[*].id", hasItem(completada.id().intValue())));
    }

    @Test
    void filtraPorEstado() throws Exception {
        Tarea pendiente = repository.crear(Tarea.nueva("Pendiente", null));
        Tarea completada = repository.crear(completada("Completada"));

        mockMvc.perform(get("/tareas").param("estado", "pendiente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].estado", everyItem(is("pendiente"))))
                .andExpect(jsonPath("$[*].id", hasItem(pendiente.id().intValue())))
                .andExpect(jsonPath("$[*].id", not(hasItem(completada.id().intValue()))));
    }

    @Test
    void filtroDeEstadoNoDistingueMayusculas() throws Exception {
        Tarea completada = repository.crear(completada("Completada"));

        mockMvc.perform(get("/tareas").param("estado", "COMPLETADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].estado", everyItem(is("completada"))))
                .andExpect(jsonPath("$[*].id", hasItem(completada.id().intValue())));
    }

    @Test
    void rechazaEstadoDesconocido() throws Exception {
        mockMvc.perform(get("/tareas").param("estado", "archivada"))
                .andExpect(status().isBadRequest());
    }

    // --- GET /tareas/{id} ---

    @Test
    void devuelveUnaTarea() throws Exception {
        Tarea tarea = repository.crear(Tarea.nueva("Leer", "Capítulo 3"));

        mockMvc.perform(get("/tareas/{id}", tarea.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tarea.id()))
                .andExpect(jsonPath("$.titulo").value("Leer"))
                .andExpect(jsonPath("$.descripcion").value("Capítulo 3"))
                .andExpect(jsonPath("$.estado").value("pendiente"));
    }

    @Test
    void devuelve404SiNoExiste() throws Exception {
        mockMvc.perform(get("/tareas/{id}", 999_999))
                .andExpect(tareaNoEncontrada(999_999))
                .andExpect(jsonPath("$.instance").value("/tareas/999999"));
    }

    @Test
    void devuelve400SiElIdNoEsNumerico() throws Exception {
        mockMvc.perform(get("/tareas/abc"))
                .andExpect(status().isBadRequest());
    }

    // --- PUT /tareas/{id} ---

    @Test
    void reemplazaLaTareaCompleta() throws Exception {
        Tarea tarea = repository.crear(Tarea.nueva("Leer", "Capítulo 3"));

        mockMvc.perform(put("/tareas/{id}", tarea.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "Leer libro", "descripcion": "Capítulo 4", "estado": "completada"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tarea.id()))
                .andExpect(jsonPath("$.titulo").value("Leer libro"))
                .andExpect(jsonPath("$.descripcion").value("Capítulo 4"))
                .andExpect(jsonPath("$.estado").value("completada"));

        assertThat(repository.buscarPorId(tarea.id()))
                .contains(new Tarea(tarea.id(), "Leer libro", "Capítulo 4", EstadoTarea.COMPLETADA));
    }

    @Test
    void putSinDescripcionLaBorra() throws Exception {
        Tarea tarea = repository.crear(Tarea.nueva("Leer", "Capítulo 3"));

        mockMvc.perform(put("/tareas/{id}", tarea.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "Leer", "estado": "pendiente"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value(nullValue()));
    }

    @Test
    void putIgnoraElIdDelCuerpo() throws Exception {
        Tarea tarea = repository.crear(Tarea.nueva("Leer", null));

        mockMvc.perform(put("/tareas/{id}", tarea.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": 999999, "titulo": "Leer", "estado": "pendiente"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tarea.id()));

        assertThat(repository.buscarPorId(999_999L)).isEmpty();
    }

    @Test
    void putDevuelve404SiNoExisteYNoLaCrea() throws Exception {
        mockMvc.perform(put("/tareas/{id}", 888_888)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "Fantasma", "estado": "pendiente"}
                                """))
                .andExpect(tareaNoEncontrada(888_888))
                .andExpect(jsonPath("$.instance").value("/tareas/888888"));

        assertThat(repository.buscarPorId(888_888L)).isEmpty();
    }

    @Test
    void putRechazaTituloVacio() throws Exception {
        Tarea tarea = repository.crear(Tarea.nueva("Leer", null));

        mockMvc.perform(put("/tareas/{id}", tarea.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "", "estado": "pendiente"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putRechazaEstadoAusente() throws Exception {
        Tarea tarea = repository.crear(Tarea.nueva("Leer", null));

        mockMvc.perform(put("/tareas/{id}", tarea.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "Leer"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putRechazaEstadoDesconocido() throws Exception {
        Tarea tarea = repository.crear(Tarea.nueva("Leer", null));

        mockMvc.perform(put("/tareas/{id}", tarea.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "Leer", "estado": "archivada"}
                                """))
                .andExpect(status().isBadRequest());

        assertThat(repository.buscarPorId(tarea.id())).contains(tarea);
    }

    @Test
    void putAceptaEstadoEnMayusculas() throws Exception {
        Tarea tarea = repository.crear(Tarea.nueva("Leer", null));

        mockMvc.perform(put("/tareas/{id}", tarea.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "Leer", "estado": "COMPLETADA"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("completada"));
    }

    // --- PATCH /tareas/{id}/completar ---

    @Test
    void completaUnaTarea() throws Exception {
        Tarea tarea = repository.crear(Tarea.nueva("Leer", "Capítulo 3"));

        mockMvc.perform(patch("/tareas/{id}/completar", tarea.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tarea.id()))
                .andExpect(jsonPath("$.titulo").value("Leer"))
                .andExpect(jsonPath("$.descripcion").value("Capítulo 3"))
                .andExpect(jsonPath("$.estado").value("completada"));

        assertThat(repository.buscarPorId(tarea.id()))
                .hasValueSatisfying(t -> assertThat(t.estado()).isEqualTo(EstadoTarea.COMPLETADA));
    }

    @Test
    void completarEsIdempotente() throws Exception {
        Tarea tarea = repository.crear(completada("Ya hecha"));

        mockMvc.perform(patch("/tareas/{id}/completar", tarea.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("completada"));
    }

    @Test
    void completarDevuelve404SiNoExiste() throws Exception {
        mockMvc.perform(patch("/tareas/{id}/completar", 777_777))
                .andExpect(tareaNoEncontrada(777_777))
                .andExpect(jsonPath("$.instance").value("/tareas/777777/completar"));
    }

    // --- DELETE /tareas/{id} ---

    @Test
    void eliminaUnaTareaYRespondeSinCuerpo() throws Exception {
        Tarea tarea = repository.crear(Tarea.nueva("Borrar", null));

        mockMvc.perform(delete("/tareas/{id}", tarea.id()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(repository.buscarPorId(tarea.id())).isEmpty();
        mockMvc.perform(get("/tareas/{id}", tarea.id()))
                .andExpect(tareaNoEncontrada(tarea.id()));
    }

    @Test
    void eliminarSoloBorraEsaTarea() throws Exception {
        Tarea borrada = repository.crear(Tarea.nueva("Borrar", null));
        Tarea conservada = repository.crear(Tarea.nueva("Conservar", null));

        mockMvc.perform(delete("/tareas/{id}", borrada.id()))
                .andExpect(status().isNoContent());

        assertThat(repository.buscarPorId(conservada.id())).contains(conservada);
    }

    @Test
    void eliminarDevuelve404SiNoExiste() throws Exception {
        mockMvc.perform(delete("/tareas/{id}", 666_666))
                .andExpect(tareaNoEncontrada(666_666))
                .andExpect(jsonPath("$.instance").value("/tareas/666666"));
    }

    @Test
    void eliminarDosVecesDevuelve404LaSegunda() throws Exception {
        Tarea tarea = repository.crear(Tarea.nueva("Borrar", null));

        mockMvc.perform(delete("/tareas/{id}", tarea.id()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/tareas/{id}", tarea.id()))
                .andExpect(tareaNoEncontrada(tarea.id()));
    }

    /** 404 con cuerpo application/problem+json (RFC 9457). */
    private static ResultMatcher tareaNoEncontrada(long id) {
        List<ResultMatcher> comprobaciones = List.of(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                jsonPath("$.type").doesNotExist(), // omitido = "about:blank" (RFC 9457)
                jsonPath("$.title").value("Tarea no encontrada"),
                jsonPath("$.status").value(404),
                jsonPath("$.detail").value("No existe ninguna tarea con id " + id),
                jsonPath("$.id").value(id));
        return resultado -> {
            for (ResultMatcher comprobacion : comprobaciones) {
                comprobacion.match(resultado);
            }
        };
    }

    private static Tarea completada(String titulo) {
        return new Tarea(null, titulo, null, EstadoTarea.COMPLETADA);
    }
}
