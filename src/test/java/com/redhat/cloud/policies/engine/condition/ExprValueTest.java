package com.redhat.cloud.policies.engine.condition;

import com.redhat.cloud.policies.engine.process.Event;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExprValueTest {

    @Test
    public void testListTargetWithWrongOperators() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        List<String> aList = new ArrayList<>(3);
        aList.add("b");
        aList.add("c");
        aList.add("d");
        factMap.put("a", aList);
        event.setFacts(factMap);

//        String expr = "facts.a = 'b'";
//        assertFalse(ExprParser.evaluate(event, expr));

        String expr = "facts.a > 3";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

//    @Test
    // NotImplementedYet
    public void matchingArray() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        List<String> aList = new ArrayList<>(3);
        aList.add("b");
        aList.add("c");
        aList.add("d");
        factMap.put("a", aList);
        event.setFacts(factMap);

        String expr = "facts.a = [b, c, d]";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a = [b, c]";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "facts.a = [b, c, a]";
        assertFalse(ConditionParser.evaluate(event, expr));

        // Case-insensitive matching must work inside arrays also
        expr = "facts.a = [b, C, d]";
        assertTrue(ConditionParser.evaluate(event, expr));
    }

    @Test
    public void containsInString() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        factMap.put("a", "b c");
        event.setFacts(factMap);

        String expr = "facts.a contains \"b\"";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a contains ['b', 'c']";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a contains ['b']";
        assertTrue(ConditionParser.evaluate(event, expr));

        // Case-insensitive
        expr = "facts.a contains ['B']";
        assertTrue(ConditionParser.evaluate(event, expr));

        // Empty array matches always
        expr = "facts.a contains []";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a contains ['b', 'e']";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

    @Test
    public void containsInArray() {
        Event event = new Event();
        Map<String, Object> factMap = new HashMap<>();
        List<String> aList = new ArrayList<>(3);
        aList.add("b");
        aList.add("c");
        aList.add("d");
        factMap.put("a", aList);
        event.setFacts(factMap);

        String expr = "facts.a contains 'b'";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a contains [\"b\", \"c\"]";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a contains ['b']";
        assertTrue(ConditionParser.evaluate(event, expr));

        // Case-insensitive
        expr = "facts.A contains ['B']";
        assertTrue(ConditionParser.evaluate(event, expr));

        // Empty array matches always
        expr = "facts.a contains []";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "facts.a contains ['b', 'e']";
        assertFalse(ConditionParser.evaluate(event, expr));
    }

    @Test
    public void multiKeyTagsMatching() {
        Event event = new Event();
        event.addTag("a", "b");

        String expr = "tags.a = 'b'";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "tags.a contains 'b'";
        assertTrue(ConditionParser.evaluate(event, expr));

        event.addTag("a", "c");

        expr = "tags.a = 'b'";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "tags.a contains 'b'";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "tags.a = 'c'";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "tags.a contains 'c'";
        assertTrue(ConditionParser.evaluate(event, expr));

        event.addTag("b", "d");

        expr = "tags.b = 'd' and tags.a = 'c'";
        assertTrue(ConditionParser.evaluate(event, expr));

        expr = "tags.b = 'd' and tags.a contains 'c'";
        assertTrue(ConditionParser.evaluate(event, expr));

        // Tag parsing makes them lowerCase, but this test does not use that lowerCase parsing part - so we
        // need to add them in lower case
        event.addTag("cost center", "PnT");

        event.addTag("owner", "Jerome Marc");
        expr = "tags.owner contains 'Jerome'";
        assertTrue(ConditionParser.evaluate(event, expr));

        event.addTag("owner", "Michael Burman");
        expr = "tags.owner contains 'Jerome'";
        assertTrue(ConditionParser.evaluate(event, expr));

        // There's no Cost, only 'Cost Center'
        expr = "tags.cost";
        assertFalse(ConditionParser.evaluate(event, expr));

        expr = "tags.owner IN ['Jerome Marc', 'Thomas Heute']";
        assertTrue(ConditionParser.evaluate(event, expr));
    }
}
