package org.hawkular.alerts.engine.impl.ispn.model;

import com.google.common.collect.Multimap;
import org.apache.lucene.document.Document;
import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;
import org.hibernate.search.bridge.ContainerBridge;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.StringBridge;

import java.util.Map;

/**
 * This bridge adds all the tags as fields in the index with a prefix "tags."
 */
public class TagsBridge implements ContainerBridge, FieldBridge {
    private static final MsgLogger log = MsgLogging.getMsgLogger(TagsBridge.class);

    private static String TAGS_PREFIX = "tags.";

    TagBridge bridge;

    public TagsBridge() {
        bridge = new TagBridge();
    }

    @Override
    public FieldBridge getElementBridge() {
        return bridge;
    }

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        Multimap<String, String> tags = (Multimap<String, String>) value;
        for (Map.Entry<String, String> tagEntry : tags.entries()) {
            bridge.set(TAGS_PREFIX + tagEntry.getKey(), tagEntry.getValue(), document, luceneOptions);
        }
    }

    public static class TagBridge implements FieldBridge, StringBridge {
        @Override
        public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
            luceneOptions.addFieldToDocument(name, objectToString(value), document);
        }

        @Override
        public String objectToString(Object o) {
            return (String) o;
        }
    }
}
