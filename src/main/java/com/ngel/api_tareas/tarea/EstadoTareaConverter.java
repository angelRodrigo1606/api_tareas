package com.ngel.api_tareas.tarea;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** Permite usar ?estado=pendiente (sin distinguir mayúsculas) en parámetros de la URL. */
@Component
class EstadoTareaConverter implements Converter<String, EstadoTarea> {

    @Override
    public EstadoTarea convert(String source) {
        return EstadoTarea.desde(source);
    }
}
