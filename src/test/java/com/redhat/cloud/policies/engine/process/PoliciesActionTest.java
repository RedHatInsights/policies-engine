package com.redhat.cloud.policies.engine.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PoliciesActionTest {

    PoliciesAction testee;

    @BeforeEach
    void setUp() {
        testee = new PoliciesAction(false);
    }

    @Test
    void shouldReturnOrgIdAsPartOfKey() {
        testee.setAccountId("someAccountId");

        final String key = testee.getKey();

        assertEquals("someAccountIdnull", key);
    }

    @Test
    void shouldContainAccountIfAsPartOfKeyWhen() {
        testee = new PoliciesAction(true);
        testee.setOrgId("someOrgId");

        final String key = testee.getKey();

        assertEquals("someOrgIdnull", key);
    }

    @Test
    void shouldContainOrgIdWhenAccountIdAndOrgIdArePresent() {
        testee = new PoliciesAction(true);

        testee.setAccountId("someAccountId");
        testee.setOrgId("someOrgId");

        final String key = testee.getKey();

        assertEquals("someOrgIdnull", key);
    }

    @Test
    void shouldContainOrgIdWhenOrgIdIsNull() {
        testee.setAccountId("someAccountId");
        testee.setOrgId(null);

        final String key = testee.getKey();

        assertEquals("someAccountIdnull", key);
    }

    @Test
    void shouldContainOrgIdWhenOrgIdIsEmpty() {
        testee.setAccountId("someAccountId");
        testee.setOrgId("");

        final String key = testee.getKey();

        assertEquals("someAccountIdnull", key);
    }
}
