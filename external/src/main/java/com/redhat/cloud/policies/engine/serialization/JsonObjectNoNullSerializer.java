package com.redhat.cloud.policies.engine.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import io.vertx.core.json.*;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

public class JsonObjectNoNullSerializer implements Serializer<JsonObject> {
    private static final ObjectMapper mapper;

    static {
        mapper = getJsonObjectMapper();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, JsonObject entries) {
        try {
            return entries == null ? null : mapper.writeValueAsBytes(entries);
        } catch(Exception e) {
            throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
        }
    }

    @Override
    public byte[] serialize(String topic, Headers headers, JsonObject data) {
        return serialize(topic, data);
    }

    @Override
    public void close() {

    }

    public static ObjectMapper getJsonObjectMapper() {
        DefaultSerializerProvider.Impl sp = new DefaultSerializerProvider.Impl();
        sp.setNullValueSerializer(new NullSerializer());

        SimpleModule module = new SimpleModule();
        module.addSerializer(JsonObject.class, new JsonObjectSerializer());
        module.addSerializer(JsonArray.class, new JsonArraySerializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializerProvider(sp);
        mapper.registerModule(module);

        return mapper;
    }

    private static class NullSerializer extends JsonSerializer<Object> {
        public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString("");
        }
    }

    /*
        These are marked package-private in Vert.X, so we need to copy them
    */

    static class JsonObjectSerializer extends JsonSerializer<JsonObject> {
        JsonObjectSerializer() {
        }

        public void serialize(JsonObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeObject(value.getMap());
        }
    }

    static class JsonArraySerializer extends JsonSerializer<JsonArray> {
        JsonArraySerializer() {
        }

        public void serialize(JsonArray value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeObject(value.getList());
        }
    }
}
