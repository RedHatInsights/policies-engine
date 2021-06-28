package com.redhat.cloud.policies.engine.history.alert.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.json.Json;
import io.vertx.core.json.jackson.JacksonCodec;
import org.hawkular.alerts.api.model.Lifecycle;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

@Converter
public class LifecyclesConverter implements AttributeConverter<List<Lifecycle>, String> {

    @Override
    public String convertToDatabaseColumn(List<Lifecycle> attribute) {
        return Json.encode(attribute);
    }

    @Override
    public List<Lifecycle> convertToEntityAttribute(String dbData) {
        return JacksonCodec.decodeValue(dbData, new TypeReference<>() {});
    }
}
