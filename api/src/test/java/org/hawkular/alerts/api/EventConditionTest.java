package org.hawkular.alerts.api;

import org.hawkular.alerts.api.model.condition.EventCondition;
import org.hawkular.alerts.api.model.event.Event;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class EventConditionTest {

    @Test
    public void testCategoryExpressionWithSpaces() {

        EventCondition condition = new EventCondition("tenant", "trigger-1", "app.war", "category = 'my category'");
        Event event1 = new Event();
        event1.setCategory("my category");

        assertTrue(condition.match(event1));

        Event event2 = new Event();
        event2.setCategory("my category 2");

        assertFalse(condition.match(event2));

//        condition.setExpression("category starts 'my category '");
//
//        assertTrue(condition.match(event2));
//
//        condition.setExpression("category ends '2'");
//
//        assertTrue(condition.match(event2));
    }

    @Test
    public void testCtimeExpression() {
        EventCondition condition = new EventCondition("tenant", "trigger-1", "app.war", "ctime > 10");

        Event event1 = new Event();
        event1.setCtime(11);

        assertTrue(condition.match(event1));

        Event event2 = new Event();
        event2.setCtime(9);

        assertFalse(condition.match(event2));

        condition.setExpression("ctime = 10");

        Event event3 = new Event();
        event3.setCtime(10);

        assertTrue(condition.match(event3));

        condition.setExpression("ctime != 10");

        assertFalse(condition.match(event3));
    }

    @Test
    public void testTagExpression() {
        EventCondition condition = new EventCondition("tenant", "trigger-1", "app.war", "tags.server = 'MyServer'");

        Event event1 = new Event();
        event1.addTag("server", "MyServer");

        assertTrue(condition.match(event1));

        condition.setExpression("tags.server != 'MyServer'");

        assertFalse(condition.match(event1));

        condition.setExpression("tags.quantity >= 11");

        event1.addTag("quantity", "11");

        assertTrue(condition.match(event1));

        event1.addTag("quantity", "12");

        assertTrue(condition.match(event1));

        event1.addTag("quantity", "10");

        // This will match since the 11 and 12 are stored also. addTag does not replace tag
        assertTrue(condition.match(event1));

//        condition.setExpression("tags.log.category starts 'WARN'");
//
//        event1.addTag("log.category", "WARNING");
//
//        assertTrue(condition.match(event1));
    }

    @Test
    public void testComposeExpression() {
        EventCondition condition = new EventCondition("tenant", "trigger-1", "bpm",
                "tags.url = '/foo/bar' AND tags.method = 'GET' AND tags.threshold <= 100");
        Event bpmEvent1 = new Event();
        bpmEvent1.setDataId("bpm");
        bpmEvent1.addTag("url", "/foo/bar");
        bpmEvent1.addTag("method", "GET");
        bpmEvent1.addTag("threshold", "45");

        assertTrue(condition.match(bpmEvent1));

        Event bpmEvent2 = new Event();
        bpmEvent2.setDataId("bpm");
        bpmEvent2.addTag("url", "/foo/bar");
        bpmEvent2.addTag("method", "GET");
        bpmEvent2.addTag("threshold", "101");

        assertFalse(condition.match(bpmEvent2));
    }

    @Test
    public void testFacts() {
        EventCondition condition = new EventCondition("tenant", "trigger-1", "app.war",
                "facts.server = 'MyServer' AND facts.insanity.mylife = 'gone'");

        Event event1 = new Event();
        Map<String, Object> factsMap = new HashMap<>();
        Map<String, String> insanityMap = new HashMap<>();
        insanityMap.put("mylife", "gone");
        factsMap.put("insanity", insanityMap);
        factsMap.put("server", "MyServer");
        event1.setFacts(factsMap);

        assertTrue(condition.match(event1));

        EventCondition condition2 = new EventCondition("tenant", "trigger-1", "app.war",
                "(facts.server = 'MyServer' AND facts.insanity.mylife = 'gone' AND facts.this.could.go.deep = 'no')");

        assertFalse(condition2.match(event1));
    }
}
