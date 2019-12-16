package org.hawkular.alerts.engine.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hawkular.alerts.engine.impl.AlertsEngineCache.DataEntry;
import org.junit.Test;

/**
 * Testing AlertsEngineCache for caching.
 *
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class AlertsEngineCacheTest {

    @Test
    public void basicTest() {
        AlertsEngineCache cache = new AlertsEngineCache();

        DataEntry entry1 = new DataEntry("o1", "t1", "d1");
        DataEntry entry2 = new DataEntry("o1", "t1", "d2");
        DataEntry entry3 = new DataEntry("o1", "t2", "d3");
        DataEntry entry4 = new DataEntry("o1", "t4", "d1");

        cache.add(entry1);
        assertTrue(cache.isDataIdActive("o1", "d1"));
        cache.add(entry2);
        assertTrue(cache.isDataIdActive("o1", "d2"));
        cache.add(entry3);
        assertTrue(cache.isDataIdActive("o1", "d3"));
        cache.add(entry4);

        cache.remove("o1", "t1");
        assertTrue(cache.isDataIdActive("o1","d1"));
        cache.remove("o1", "t4");
        assertFalse(cache.isDataIdActive("o1", "d1"));
        cache.remove("o1", "t2");
        assertFalse(cache.isDataIdActive("o1", "d3"));
    }

}
