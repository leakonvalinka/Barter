package at.ac.ase.inso.group02.util;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * provides a custom object mapper to use for (de-)serialization of JSON payloads
 * that includes the necessary modules (notably JtsModule for Geometry data, which is not registered by default)
 */
@ApplicationScoped
public class ObjectMapperProducer {

    @Produces
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new JtsModule());
    }
}