package at.ac.ase.inso.group02.util;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Base64;


public class MapperUtil {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new JtsModule())
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> T map(Object source, Class<T> targetClass) {
        return mapper.convertValue(source, targetClass);
    }

    public static <T> T map(Object source, TypeReference<T> typeReference) {
        return mapper.convertValue(source, typeReference);
    }

    public static String convertToJson(Object source) throws JsonProcessingException {
        return mapper.writeValueAsString(source);
    }

    public static byte[] decodeBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    public static <T> void updateEntity(T entity, Object updateSource) throws JsonMappingException {
        mapper.updateValue(entity, updateSource);
    }
}

