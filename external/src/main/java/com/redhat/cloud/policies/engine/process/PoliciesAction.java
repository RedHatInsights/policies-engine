package com.redhat.cloud.policies.engine.process;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PoliciesAction {
    private String accountId;
    private LocalDateTime timestamp;
    private Context context = new Context();
    private Set<Event> events = new HashSet<>();

    public String getKey() {
        return accountId + context.systemCheckIn;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Set<Event> getEvents() {
        return events;
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Payload {
        private String policyId;
        private String policyName;
        private String policyDescription;
        private String policyCondition;

        public String getPolicyId() {
            return policyId;
        }

        public void setPolicyId(String policyId) {
            this.policyId = policyId;
        }

        public String getPolicyName() {
            return policyName;
        }

        public void setPolicyName(String policyName) {
            this.policyName = policyName;
        }

        public String getPolicyDescription() {
            return policyDescription;
        }

        public void setPolicyDescription(String policyDescription) {
            this.policyDescription = policyDescription;
        }

        public String getPolicyCondition() {
            return policyCondition;
        }

        public void setPolicyCondition(String policyCondition) {
            this.policyCondition = policyCondition;
        }
    }

    public static class Event {
        private Payload payload = new Payload();

        public Payload getPayload() {
            return payload;
        }

        public void setPayload(Payload payload) {
            this.payload = payload;
        }
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Context {

        private static class IsoDateTimeSerializer extends LocalDateTimeSerializer {
            @Override
            protected DateTimeFormatter _defaultFormatter() {
                return DateTimeFormatter.ISO_DATE_TIME;
            }
        }

        private static class TagsSerializer extends JsonSerializer<HashMap<String, Set<String>>> {
            @Override
            public void serialize(HashMap<String, Set<String>> tags, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeObject(tags.entrySet().stream().map(tagsSet -> tagsSet.getValue().stream().map(tagValue -> {
                    Map<String, String> tag = new HashMap<>();
                    tag.put("key", tagsSet.getKey());
                    tag.put("value", tagValue);
                    return tag;
                }).collect(Collectors.toList())).flatMap(List::stream).collect(Collectors.toList()));
            }
        }

        private String inventoryId;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @JsonSerialize(using = IsoDateTimeSerializer.class)
        private LocalDateTime systemCheckIn;

        private String displayName;

        @JsonSerialize(using = TagsSerializer.class)
        private HashMap<String, Set<String>> tags;

        public Context() {
            tags = new HashMap<>();
        }

        public String getInventoryId() {
            return inventoryId;
        }

        public void setInventoryId(String inventoryId) {
            this.inventoryId = inventoryId;
        }

        public LocalDateTime getSystemCheckIn() {
            return systemCheckIn;
        }

        public void setSystemCheckIn(LocalDateTime systemCheckIn) {
            this.systemCheckIn = systemCheckIn;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public HashMap<String, Set<String>> getTags() {
            return tags;
        }

        public void setTags(HashMap<String, Set<String>> tags) {
            this.tags = tags;
        }
    }
}
