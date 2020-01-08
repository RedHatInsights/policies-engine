package org.hawkular.alerts.rest

import io.quarkus.test.junit.QuarkusTest
import org.hawkular.alerts.api.model.dampening.Dampening
import org.hawkular.alerts.api.model.dampening.Dampening.Type
import org.hawkular.alerts.api.model.trigger.Mode
import org.hawkular.alerts.api.model.trigger.Trigger
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * Dampening REST tests.
 *
 * @author Lucas Ponce
 */
@QuarkusTest
class DampeningITest extends AbstractQuarkusITestBase {

    @Test
    void createDampening() {
       Trigger testTrigger = new Trigger("test-trigger-6", "No-Metric");

        // make sure clean test trigger exists
        client.delete(path: "triggers/test-trigger-6")
        def resp = client.post(path: "triggers", body: testTrigger)
        assertEquals(200, resp.status)

        Dampening d = Dampening.forRelaxedCount("", "test-trigger-6", Mode.FIRING, 1, 2);

        resp = client.post(path: "triggers/test-trigger-6/dampenings", body: d)
        assertEquals(200, resp.status)

        d = resp.data

        resp = client.get(path: "triggers/test-trigger-6/dampenings/" + d.getDampeningId());
        assertEquals(200, resp.status)
        assertEquals("RELAXED_COUNT", resp.data.type)

        d.setType(Type.STRICT)
        resp = client.put(path: "triggers/test-trigger-6/dampenings/" + d.getDampeningId(), body: d)
        assertEquals(200, resp.status)

        resp = client.get(path: "triggers/test-trigger-6/dampenings/" + d.getDampeningId())
        assertEquals(200, resp.status)
        assertEquals("STRICT", resp.data.type)

        resp = client.get(path: "triggers/test-trigger-6/dampenings")
        assertEquals(200, resp.status)
        assertEquals(1, resp.data.size())

        resp = client.get(path: "triggers/test-trigger-6/dampenings/mode/FIRING")
        assertEquals(200, resp.status)
        assertEquals(1, resp.data.size())
        assertEquals("test-trigger-6", resp.data[0].triggerId)

        resp = client.delete(path: "triggers/test-trigger-6/dampenings/" + d.getDampeningId())
        assertEquals(200, resp.status)

        client.delete(path: "triggers/test-trigger-6")
        assertEquals(200, resp.status)
    }

}
