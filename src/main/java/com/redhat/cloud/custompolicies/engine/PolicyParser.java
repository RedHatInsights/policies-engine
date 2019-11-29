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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hrupp
 */
class PolicyParser {

  private String condition_string;
  private String actions;

  PolicyParser(String policy) {
    validate(policy);
    evaluateConditions(new HashMap<>());
  }

  PolicyParser(Policy policy) {
    if (policy.conditions == null || policy.conditions.isEmpty()) {
      throw new IllegalArgumentException("No conditions passed");
    }
    if (!policy.conditions.equals("true") && findComparator(policy.conditions)==-1) {
      throw new IllegalArgumentException("No comparator found in conditions");
    }
    if (policy.actions == null || policy.actions.isEmpty()) {
      throw new IllegalArgumentException("No actions passed");
    }

    this.condition_string = policy.conditions;
    this.actions = policy.actions;

    evaluateConditions(new HashMap<>());

  }


  private void validate(String policy) {
    if (!policy.startsWith("IF ")) {
      throw new IllegalArgumentException("Policy does not start with 'IF '");
    }
    if (!policy.contains(" THEN ")) {
      throw new IllegalArgumentException("Policy does not contain a ' THEN ' clause");
    }
    int pos = policy.indexOf(" THEN ");
    condition_string = policy.substring(3, pos).trim();

    if (condition_string.isEmpty()) {
      throw new IllegalArgumentException("No conditions found for policy >" + policy + "<");
    }
    if (!condition_string.equals("true") && findComparator(condition_string)==-1) {
      throw new IllegalArgumentException("No comparator found in conditions");
    }

    actions = policy.substring(pos+ 6);

    if (actions.isEmpty()) {
      throw new IllegalArgumentException("No actions found for policy >" + policy + "<");
    }
  }

  /**
   * Evaluate the facts vs the given conditions
   * @param facts
   * @return
   */
  boolean evaluateConditions(Map<String, Object> facts) {
    return evaluateConditions(facts,condition_string);
  }

  private boolean evaluateConditions(Map<String, Object> facts, String expression) {

    if (facts==null) {
      return false;
    }

    if (expression.contains("(")) {
      // We evaluate left to right
      int lpos = expression.indexOf("(");
      int rpos = expression.lastIndexOf(")");

      String subExpression = expression.substring(lpos+1,rpos);
      boolean inParens = evaluateConditions(facts,subExpression);
      if (lpos==0 && rpos == expression.length()-1) {
        // This consumed the entire expression
        return inParens;
      }

      if (lpos==0) {
        // Parens start left, check if we have anything right to the closing one
        if (expression.length() > rpos) {
          subExpression = inParens + " " + expression.substring(rpos+1);
          return evaluateConditions(facts, subExpression);
        }
      } else { // TODO parens in the middle
        // Parens are on the right
        subExpression = expression.substring(0,lpos-1) + " " + inParens;
        return evaluateConditions(facts,subExpression);
      }
    }

    int pos;
    Oper op = Oper.NONE;
    pos = expression.indexOf(" OR ");
    if (pos != -1 ) {
      op = Oper.OR;
    } else {
      pos = expression.indexOf(" AND ");
      if (pos != -1 ) {
        op = Oper.AND;
      }
    }

    if (!op.equals(Oper.NONE)) {
      String cond_left = expression.substring(0, pos);
      String cond_right = expression.substring(pos + (op.equals(Oper.OR) ? 4 : 5));
      boolean res_left = evalOneCondition(facts,cond_left);
      boolean res_right = evalOneCondition(facts, cond_right);

      boolean result = op.equals(Oper.AND) ? res_left && res_right : res_left || res_right;
      return result;
    }
    return evalOneCondition(facts, condition_string);
  }

  private boolean evalOneCondition(Map<String, Object> facts, String condition) {

    condition = condition.trim();

    if (condition.equalsIgnoreCase("true")) {
      return true;
    }

    if (condition.equalsIgnoreCase("false")) {
      return false;
    }

    // Simple case for now  Fact compare value
    int pos = findComparator(condition);
    if (pos == -1 ) {
      throw new IllegalStateException("No comparator found");
    }
    String factName = condition.substring(0, pos);
    factName= stripQuoteSigns(factName);
    int afterComparatorIndex = condition.indexOf(" ", pos + 1);
    if (afterComparatorIndex == -1) {
      throw new IllegalStateException("Comparision lacking right side");
    }
    String comparatorString = condition.substring(pos, afterComparatorIndex);
    String value = condition.substring(pos+comparatorString.length()+1);

    boolean isStringValue = false;
    if (value.contains("\"")) {
      isStringValue = true;
      value = stripQuoteSigns(value);
    }

    if (!facts.containsKey(factName)) {
      return false;
    }
    Object factValue = facts.get(factName);

    Comparator comparator = Comparator.fromString(comparatorString);

    boolean result;
    if (factValue instanceof String[]) {
      String[] valueArray = (String[]) factValue;
      if (comparator.equals(Comparator.CONTAINS)) {
        for (String item : valueArray) {
          if (value.equals(item)) {
            return true;
          }
        }
      }
      return false;
    }
    else if (isStringValue) {
      result = handleStringValue(value,(String)factValue,comparator);
    }
    else {
      result = handleIntValue(Integer.parseInt(value),factValue,comparator);
    }

    return result;
  }

  private boolean handleIntValue(int numValue, Object factValue, Comparator comparator) {
    int valFromFact;

    if (factValue instanceof BigDecimal) {
      valFromFact =((BigDecimal) factValue).intValueExact();
    } else {
      valFromFact = (Integer)factValue;
    }

    switch (comparator) {
      case EQUALS:
          return numValue == valFromFact;
      case NOT_EQUALS:
        return numValue != valFromFact;
      case LESS:
        return valFromFact < numValue;
      case MORE:
        return valFromFact > numValue;
      case LESS_THAN:
        return valFromFact <= numValue;
      case MORE_THAN:
        return valFromFact >= numValue;
      default:
        return false;
    }
  }

  private boolean handleStringValue(String value, String factValue, Comparator comparator) {
    switch (comparator) {
      case EQUALS:
        return value.equals(factValue);
      case NOT_EQUALS:
        return !(value.equals(factValue));
      case MORE:
        return factValue.startsWith(value) && !factValue.equals(value);
      case MORE_THAN:
        return factValue.startsWith(value);
      case CONTAINS:
        return factValue.contains(value);
      default:
        return false;
    }
  }



  // We need to remove spaces + " around the fact name
  private String stripQuoteSigns(String factName) {
    String t = factName.trim();
    t = t.replace("\"","");
    return t;
  }

  private int findComparator(String expression) {
    for (Comparator c : Comparator.values()) {
      if (expression.contains(c.comparator)) {
        return expression.indexOf(c.comparator);
      }
     }

    return -1;
  }

  public String getCondition_string() {
    return condition_string;
  }

  public String getActions() {
    return actions;
  }

  private enum Comparator {
    EQUALS("=="),
    NOT_EQUALS("!="),
    LESS("<"),
    MORE(">"),
    LESS_THAN("<="),
    MORE_THAN(">="),
    CONTAINS("?"),   // Case sensitive
    ;

    String comparator;

    Comparator(String comparator) {
      this.comparator = comparator;
    }

    public static Comparator fromString(String comparatorString) {
      for (Comparator c : Comparator.values()) {
        if (c.comparator.equalsIgnoreCase(comparatorString)) {
          return c;
        }
      }
      throw new IllegalArgumentException("Unknown comparator " + comparatorString);
    }

    public String getComparator() {
      return comparator;
    }
  }

  private enum Oper { // TODO need better name
    NONE,
    AND,
    OR;
  }
}
