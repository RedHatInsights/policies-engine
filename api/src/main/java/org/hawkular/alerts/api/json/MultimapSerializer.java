package org.hawkular.alerts.api.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.Map;

/**
 * Serializes the multimap structure to a json like map with same key potentially appearing multiple times. Does not use jackson-datatype-guava package,
 * because the layout it provides is {"key": ["value", "value2"]} while we do not want to break the compatibility to existing data. As such, we allow the
 * input to be in the format of: [{"key": "value"}, {"key": "value2"}]. For convenience, that same struct is used in the insights reports
 * also providing better inter compatibility with other insights applications.
 */
public class MultimapSerializer extends JsonSerializer<Multimap<String, String>> {
    @Override
    public void serialize(Multimap<String, String> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        for (Map.Entry<String, String> entry : map.entries()) {
            if(entry.getValue() != null && entry.getValue().length() > 0) {
                System.out.printf("Adding: %s\n", entry.getValue());
                jsonGenerator.writeStringField(entry.getKey(), entry.getValue());
            }
        }
        jsonGenerator.writeEndObject();
    }
}
