package com.redhat.cloud.policies.engine.history.alert.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Multimap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@ApplicationScoped
@Converter
public class TagsConverter implements AttributeConverter<Multimap<String, String>, String> {

    @Inject
    ObjectMapper objectMapper;

    @PostConstruct
    void postConstruct() {
        objectMapper.registerModule(new GuavaModule());
    }

    @Override
    public String convertToDatabaseColumn(Multimap<String, String> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Multimap serialization failed", e);
        }
    }

    @Override
    public Multimap<String, String> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Multimap deserialization failed", e);
        }
    }
}
