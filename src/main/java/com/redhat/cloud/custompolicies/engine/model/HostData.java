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
package com.redhat.cloud.custompolicies.engine.model;

import java.util.List;
import java.util.Map;

/**
 * @author hrupp
 */
public class HostData {

      public String id; // = fields.UUID()
      String display_name; //  = fields.Str()
      String ansible_host; //  = fields.Str()
      public String account; //  = fields.Str(required=True)
      String insights_id; //  = fields.Str()
      String rhel_machine_id; //  = fields.Str()
      String subscription_manager_id; //  = fields.Str()
      String satellite_id; //  = fields.Str()
      String fqdn; //  = fields.Str()
      String bios_uuid; //  = fields.Str()
      List<String> ip_addresses; //  = fields.List(fields.Str())
      List<String> mac_addresses; //  = fields.List(fields.Str())
      public Map<String, Object> facts;
      String external_id; //  = fields.Str()
//      # FIXME:
//      # created = fields.DateTime(format="iso8601")
//      # updated = fields.DateTime(format="iso8601")
//      # FIXME:
     String created; //  = fields.Str()
      String updated; //  = fields.Str()

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("HostData{");
    sb.append("id='").append(id).append('\'');
    sb.append(", display_name='").append(display_name).append('\'');
    sb.append(", ansible_host='").append(ansible_host).append('\'');
    sb.append(", account='").append(account).append('\'');
    sb.append(", insights_id='").append(insights_id).append('\'');
    sb.append(", rhel_machine_id='").append(rhel_machine_id).append('\'');
    sb.append(", subscription_manager_id='").append(subscription_manager_id).append('\'');
    sb.append(", satellite_id='").append(satellite_id).append('\'');
    sb.append(", fqdn='").append(fqdn).append('\'');
    sb.append(", bios_uuid='").append(bios_uuid).append('\'');
    sb.append(", ip_addresses=").append(ip_addresses);
    sb.append(", mac_addresses=").append(mac_addresses);
    sb.append(", external_id='").append(external_id).append('\'');
    sb.append(", created='").append(created).append('\'');
    sb.append(", updated='").append(updated).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
