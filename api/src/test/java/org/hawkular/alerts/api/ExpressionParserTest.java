/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.alerts.api;

import org.hawkular.alerts.api.model.condition.EventCondition;
import org.hawkular.alerts.api.model.event.Event;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpressionParserTest {

    @Test
    public void testParens2() {
        String expr = "true AND (\"cores\" == 1  OR \"rhelversion\" > \"8\")";

        EventCondition cond = new EventCondition();
        cond.setExpr(expr);

        Event event1 = new Event();
        Map<String,Object> facts = new HashMap<>();
        facts.put("cores",1);
        facts.put("rhelversion","8.1");

        event1.setFacts(facts);
        assertTrue(cond.match(event1));
    }

}
