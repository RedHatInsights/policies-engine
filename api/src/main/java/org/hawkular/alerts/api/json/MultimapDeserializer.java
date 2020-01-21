package org.hawkular.alerts.api.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.Iterator;

/**
 * Deserializes a map structure with multiple same keys to a multimap. Does not use jackson-datatype-guava package, because the layout
 * it provides is {"key": ["value", "value2"]} while we do not want to break the compatibility to existing data. As such, we allow the
 * input to be in the format of: [{"key": "value"}, {"key": "value2"}]. For convenience, that same struct is used in the insights reports
 * also providing better inter compatibility with other insights applications.
 */
public class MultimapDeserializer extends JsonDeserializer<Multimap<String, String>> {
    @Override
    public Multimap<String, String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec objectCodec = jsonParser.getCodec();
        JsonNode mapNode = objectCodec.readTree(jsonParser);

        LinkedHashMultimap<String, String> map = LinkedHashMultimap.create();
        Iterator<String> fieldNames = mapNode.fieldNames();
        while (fieldNames.hasNext()) {
            String field = fieldNames.next();
            map.put(field, mapNode.get(field).asText());
        }
        return map;
    }
}
