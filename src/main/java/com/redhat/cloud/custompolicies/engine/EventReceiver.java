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

import com.redhat.cloud.custompolicies.engine.model.HostData;
import com.redhat.cloud.custompolicies.engine.model.HostEventModel;
import com.redhat.cloud.custompolicies.engine.model.Notifier;
import com.redhat.cloud.custompolicies.engine.model.Policy;
import io.smallrye.reactive.messaging.annotations.Merge;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

/**
 * @author hrupp
 */
@ApplicationScoped
public class EventReceiver {

  PolicyRetriever policyRetriever = new PolicyRetriever();

  Jsonb jsonb = JsonbBuilder.create();
  private HostData host;

  @Inject
  Notifier notifier;

  @Transactional
  @Incoming("events")
  @Merge(Merge.Mode.MERGE)
  public Message<String> processEvent(String event) {
    System.out.println("Got an event " + event);

    HostEventModel em = jsonb.fromJson(event, HostEventModel.class);

    String customer = em.host.account;
    List<Policy> policies = policyRetriever.getEnabledPoliciesForAccount(customer);

    for (Policy policy : policies) {
      PolicyParser pp = new PolicyParser(policy);
      Map<String, Object> facts = em.host.facts;
      boolean match = pp.evaluateConditions(facts);

      System.out.println("Policy " + policy.conditions + " match: " + match);
      if (match) {
        notifier.doNotify(policy);
      }
    }

    return null; // TODO allowed?
  }

}
