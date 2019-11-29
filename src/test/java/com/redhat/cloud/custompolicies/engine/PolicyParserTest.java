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
package com.redhat.cloud.custompolicies.engine;

import com.redhat.cloud.custompolicies.engine.model.Policy;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Policy Parser
 * @author hrupp
 */

public class PolicyParserTest {

  @Test
  void testSimple() {
    String policy = "IF \"cores\" == 1 THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    String conditions = parser.getCondition_string();
    Assert.assertEquals(conditions, "\"cores\" == 1", conditions);
    assert parser.getActions().equals("EMAIL");
  }

  @Test()
  void testSimpleNoTHEN() {
    String policy = "IF \"cores\" == ";

    try {
      new PolicyParser(policy);
      Assert.fail("Parsing should have failed");
    }
    catch (Exception  e) {
      // expected
    }
  }

  @Test()
  void testStructuredNoCondition() {

    Policy policy = new Policy();
    policy.name="bla";
    policy.conditions = "";
    policy.actions = "EMAIL";

    try {
      new PolicyParser(policy);
      Assert.fail("Parsing should have failed");
    }
    catch (Exception  e) {
      // expected
    }
  }

  @Test()
  void testStructuredBadCondition() {

    Policy policy = new Policy();
    policy.name="bla";
    policy.conditions = "\"a\" ==";
    policy.actions = "EMAIL";

    try {
      PolicyParser pp = new PolicyParser(policy);
      Assert.fail("Parsing should have failed");
    }
    catch (Exception  e) {
      // expected
    }
  }

  @Test
  void testEmptyConditions() {
    String policy = "IF  THEN EMAIL";

    try {
      new PolicyParser(policy);
      Assert.fail("Parsing should have failed");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  void testSimpleConditionNoComparator() {
    String policy = "IF a THEN EMAIL";

    try {
      new PolicyParser(policy);
      Assert.fail("Parsing should have failed");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  void testSimpleConditionAlwaysTrue() {
    String policy = "IF true THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);
    Assert.assertTrue("Condition should be always true", parser.evaluateConditions(new HashMap<>()));
  }

  @Test
  void testEmptyActions() {
    String policy = "IF \"a\" > 1 THEN ";

    try {
      new PolicyParser(policy);
      Assert.fail("Parsing should have failed");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }

  @Test
  void testEvaluateSimple() {
    String policy = "IF \"cores\" == 1 THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    Map<String, Object> facts = new HashMap<>();
    facts.put("cores",1);
    boolean match = parser.evaluateConditions(facts);

    Assert.assertTrue("facts don't match", match);

  }

  @Test
  void testEvaluateSimpleString() {
    String policy = "IF \"cpu\" == \"intel\" THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    Map<String, Object> facts = new HashMap<>();
    facts.put("cores",1);
    facts.put("cpu","intel");
    boolean match = parser.evaluateConditions(facts);

    Assert.assertTrue("facts don't match", match);

  }

  @Test
  void testEvaluateSimpleString2() {
    String policy = "IF \"rhelversion\" > \"8\" THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    Map<String, Object> facts = new HashMap<>();
    facts.put("rhelversion","7");
    boolean match = parser.evaluateConditions(facts);

    Assert.assertFalse("facts do match", match);

    facts = new HashMap<>();
    facts.put("rhelversion","8");
    match = parser.evaluateConditions(facts);

    Assert.assertFalse("facts do match", match);

    facts = new HashMap<>();
    facts.put("rhelversion","8.1");
    match = parser.evaluateConditions(facts);

    Assert.assertTrue("facts don't match", match);
  }

  @Test
  void testStringContainsCaseSensitive() {

    String policy_string = "IF \"name\" ? \"hat\" THEN EMAIL";
    PolicyParser policyParser = new PolicyParser(policy_string);

    Map<String, Object> facts = new HashMap<>();
    facts.put("name","Red Hat");

    boolean match = policyParser.evaluateConditions(facts);
    Assert.assertFalse("Facts did match",match);
  }

  @Test
  void testStringContainsCaseSensitive2() {

    String policy_string = "IF \"name\" ? \"Hat\" THEN EMAIL";
    PolicyParser policyParser = new PolicyParser(policy_string);

    Map<String, Object> facts = new HashMap<>();
    facts.put("name","Red Hat");

    boolean match = policyParser.evaluateConditions(facts);
    Assert.assertTrue("Facts did not match",match);
  }


  @Test
  void testArrayContainsCaseSensitive() {

    String policy_string = "IF \"flags\" ? \"fpu\" THEN EMAIL";
    PolicyParser policyParser = new PolicyParser(policy_string);

    Map<String, Object> facts = new HashMap<>();
    String[] flags = {"fpu","8bit","foo","cat"};
    facts.put("flags",flags);

    boolean match = policyParser.evaluateConditions(facts);
    Assert.assertTrue("Facts did not match",match);
  }


  @Test
  void testOr1() {
    String expected_conditions = "\"cores\" == 1  OR \"rhelversion\" > \"8\"";
    String policy = "IF " + expected_conditions + " THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    String conditions = parser.getCondition_string();
    Assert.assertEquals(conditions, expected_conditions, conditions);
    assert parser.getActions().equals("EMAIL");
  }

  @Test
  void testEvalOr1() {
    String expected_conditions = "\"cores\" == 1  OR \"rhelversion\" > \"8\"";
    String policy = "IF " + expected_conditions + " THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    Map<String,Object> facts = new HashMap<>();
    facts.put("cores",2);
    facts.put("rhelversion","8.1");

    boolean result = parser.evaluateConditions(facts);
    Assert.assertTrue("Facts did not match",result);

  }

  @Test
  void testAnd1() {
    String expected_conditions = "\"cores\" == 1  AND \"rhelversion\" > \"8\"";
    String policy = "IF " + expected_conditions + " THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    Map<String,Object> facts = new HashMap<>();
    facts.put("cores",2);
    facts.put("rhelversion","8.1");

    boolean result = parser.evaluateConditions(facts);
    Assert.assertFalse("Facts did match",result);

  }

  @Test
  void testAnd2() {
    String expected_conditions = "\"cores\" == 1  AND \"rhelversion\" >= \"8\"";
    String policy = "IF " + expected_conditions + " THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    Map<String,Object> facts = new HashMap<>();
    facts.put("cores",2);
    facts.put("rhelversion","8.1");

    boolean result = parser.evaluateConditions(facts);
    Assert.assertFalse("Facts did match",result);
  }

  @Test
  void testAnd3() {
    String expected_conditions = "\"cores\" == 1  AND \"rhelversion\" >= \"8\" AND true";
    String policy = "IF " + expected_conditions + " THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    Map<String,Object> facts = new HashMap<>();
    facts.put("cores",2);
    facts.put("rhelversion","8.1");

    boolean result = parser.evaluateConditions(facts);
    Assert.assertFalse("Facts did match",result);
  }

  @Test
  void testAnd4() {
    String expected_conditions = "\"cores\" == 2  AND \"rhelversion\" >= \"8\" AND true";
    String policy = "IF " + expected_conditions + " THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    Map<String,Object> facts = new HashMap<>();
    facts.put("cores",2);
    facts.put("rhelversion","8.1");

    boolean result = parser.evaluateConditions(facts);
    Assert.assertFalse("Facts did match",result);
  }

  @Test
  void testAnd5() {
    String expected_conditions = "\"cores\" == 2  AND \"rhelversion\" >= \"8\" AND true";
    Policy policy = new Policy();
    policy.conditions = expected_conditions;
    policy.actions = "EMAIL roadrunner@acme.org";

    PolicyParser parser = new PolicyParser(policy);

    Map<String,Object> facts = new HashMap<>();
    facts.put("cores",2);
    facts.put("rhelversion","8.1");

    boolean result = parser.evaluateConditions(facts);
    Assert.assertFalse("Facts did match",result);
  }


  @Test
  void testParens1() {
    String expected_conditions = "\"cores\" == 1  AND \"rhelversion\" > \"8\"";
    String policy = "IF (" + expected_conditions + ") THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    Map<String,Object> facts = new HashMap<>();
    facts.put("cores",1);
    facts.put("rhelversion","8.1");

    boolean result = parser.evaluateConditions(facts);
    Assert.assertTrue("Facts did not match",result);
  }

  @Test
  void testParens2() {
    String expected_conditions = "true AND (\"cores\" == 1  OR \"rhelversion\" > \"8\")";
    String policy = "IF " + expected_conditions + " THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    Map<String,Object> facts = new HashMap<>();
    facts.put("cores",1);
    facts.put("rhelversion","8.1");

    boolean result = parser.evaluateConditions(facts);
    Assert.assertTrue("Facts did not match",result);
  }

  @Test
  void testParens3() {
    String expected_conditions = "(\"cores\" == 1  OR \"rhelversion\" > \"8\") AND true";
    String policy = "IF " + expected_conditions + " THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    Map<String,Object> facts = new HashMap<>();
    facts.put("cores",1);
    facts.put("rhelversion","8.1");

    boolean result = parser.evaluateConditions(facts);
    Assert.assertTrue("Facts did not match",result);
  }

  //@Test
  public void testParens4() {
    String expected_conditions = "true AND (\"cores\" == 1  OR \"rhelversion\" > \"8\") AND false";
    String policy = "IF " + expected_conditions + " THEN EMAIL";

    PolicyParser parser = new PolicyParser(policy);

    Map<String,Object> facts = new HashMap<>();
    facts.put("cores",1);
    facts.put("rhelversion","8.1");

    boolean result = parser.evaluateConditions(facts);
    Assert.assertFalse("Facts did match but should not",result);
  }

}
