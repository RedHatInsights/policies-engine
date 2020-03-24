package org.hawkular.alerts.api.json;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Json serialization/deserialization utility for Alerts (or Events) using Jackson implementation.
 *
 * @author Lucas Ponce
 */
public class JsonUtil {

    private static JsonUtil instance = new JsonUtil();
    private ObjectMapper mapper;
    private ObjectMapper mapperThin;

    private JsonUtil() {
        mapper = new ObjectMapper();

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.setDeserializerModifier(new JacksonDeserializer.AlertThinDeserializer());
        mapperThin = new ObjectMapper();
        mapperThin.registerModule(simpleModule);
    }

    public static String toJson(Object resource) {
        try {
            return instance.mapper.writeValueAsString(resource);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        T t;
        try {
            t = fromJson(json, clazz, false);
        } catch(Exception e) {
            Throwable cause = e.getCause();
            while(cause != null) {
                if(cause.getStackTrace()[0].getClassName().startsWith("com.redhat.cloud.policies")) {
                    throw new RuntimeException(cause.getMessage());
                }
                cause = cause.getCause();
            }
            throw e;
        }
        return t;
    }

    public static <T> Collection<T> collectionFromJson(String json, Class<T> clazz) {
        try {
            return instance.mapper.readValue(json, instance.mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            Throwable cause = e.getCause();
            while(cause != null) {
                if(cause.getStackTrace()[0].getClassName().startsWith("com.redhat.cloud.policies")) {
                    throw new RuntimeException(cause.getMessage());
                }
                cause = cause.getCause();
            }
            throw new IllegalStateException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz, boolean thin) {
        try {
            return thin ? instance.mapperThin.readValue(json, clazz) : instance.mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Object o) {
        return instance.mapper.convertValue(o, Map.class);
    }

    public static ObjectMapper getMapper() {
        return instance.mapper;
    }
}
