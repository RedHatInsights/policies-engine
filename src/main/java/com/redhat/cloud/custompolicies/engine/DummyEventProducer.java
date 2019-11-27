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
import io.reactivex.Flowable;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

/**
 * This creates an event every now and then and sends it to Kafka
 * @author hrupp
 */
@ApplicationScoped
public class DummyEventProducer {

  String msg = "{ " +
      "\"type\": \"created\"," +
      "\"timestamp\": \"snert\"," +
      "\"platform_metadata\": { }," +
      "\"host\": { }" +
      "}"
      ;

  HostEventModel hem;
  Jsonb jsonb;

  public DummyEventProducer() {

    hem = new HostEventModel();
    hem.type = "created";
    hem.timestamp = new Date().toString(); // TODO
    hem.platform_metadata = new HashMap<>();

    HostData hd = new HostData();
    hem.host = hd;

    hd.id = UUID.randomUUID().toString();
    hd.account ="1";

    jsonb = JsonbBuilder.create();

  }

//  @Outgoing("events")
  public Flowable<String> generate() {
    return Flowable.interval(30, TimeUnit.SECONDS)
    .map(tick -> {
      hem.host.facts = new HashMap<>();
      hem.host.facts.put("cores", (int)(Math.random()*8));
      if (Math.random()*10 > 3) {
        hem.host.facts.put("os_release", "7.5");
        hem.host.facts.put("arch", "x86_64");
      }
      return jsonb.toJson(hem);
    });
  }
}
