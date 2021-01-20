package org.hawkular.alerts.api;

import org.hawkular.alerts.api.model.trigger.Trigger;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.UUID;

public class TriggerTest {

    @Test
    public void notSpecifiedIdIsUUIDTest() {
        Trigger trigger = new Trigger();

        Assertions.assertDoesNotThrow(() -> { UUID.fromString(trigger.getId()); });
    }

    @Test
    public void differentIdsTest() {
        Trigger trigger1 = new Trigger();
        Trigger trigger2 = new Trigger();

        Assertions.assertNotEquals(trigger1.getId(), trigger2.getId());
    }

}
