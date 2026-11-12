package com.ngel.api_tareas.tarea;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce los errores de tareas a respuestas JSON según RFC 9457 (application/problem+json). */
@RestControllerAdvice
class TareaExceptionHandler {

    @ExceptionHandler(TareaNoEncontradaException.class)
    ProblemDetail tareaNoEncontrada(TareaNoEncontradaException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Tarea no encontrada");
        problema.setProperty("id", ex.getId());
        return problema;
    }
}
