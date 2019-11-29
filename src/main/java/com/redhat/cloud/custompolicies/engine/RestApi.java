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

import com.redhat.cloud.custompolicies.engine.model.Msg;
import com.redhat.cloud.custompolicies.engine.model.Notifier;
import com.redhat.cloud.custompolicies.engine.model.Policy;
import com.redhat.cloud.custompolicies.engine.model.SystemProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * @author hrupp
 */
@Path("/api/v1")
@Consumes("application/json")
@Produces("application/json")
public class RestApi {

  private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
  private static final String ALLOWED_ORIGINS = "*"; // TODO needs to change

  @RestClient
  HBISystemProfile hbiSystemProfile;

  @Inject
  Notifier notifier;

  // Accept a policy string
  @POST
  @Path("/verify")
  @Consumes("text/plain")
  @Produces("text/plain")
  @Operation(summary = "Verify a Policy in the Policy DSL form",
             description = "Verify a policy. The Policy is in the form 'IF <conditions> THEN <actions>'. "+
             "E.g. 'IF \"os_release\" == \"7.5\" THEN EMAIL foo@acme.org")
  @APIResponse(responseCode = "204", description = "Policy string is ok")
  @APIResponse(responseCode = "400", description = "Policy verification failed")
  public Response verifyPolicyString(String policy) {

    System.out.print("Got rule >" + policy + "< -");

    Response response ;

    try {
      new PolicyParser(policy);
      response = Response.noContent().entity("OK").build();
    } catch (IllegalArgumentException iae) {
      String msg = "Verification failed: " + iae.getMessage();
      response = Response.status(400).entity(msg).build();
    }
    return response;
  }

  @POST
  @Path("/verifyPolicy")
  @Operation(summary = "Verify a Policy in the structured form")
  @APIResponse(responseCode = "204", description = "Policy string is ok")
  @APIResponse(responseCode = "412", description = "Policy verification failed")
  public Response verifyPolicy(Policy policy) {

    Response.ResponseBuilder builder;
    try {
      new PolicyParser(policy);
      builder = Response.noContent();
    } catch (Exception ie) {
//    } catch (IllegalArgumentException|IllegalStateException ie) { // See Quarkus #5878
      Msg msg = new Msg(ie.getMessage());
      builder = Response.status(412, ie.getMessage()).entity(msg);
    }
    builder.header(ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGINS);
    return builder.build();
  }

  @POST
  @Path("/eval/{customer}")
  @Operation(summary = "Evaluate the facts against stored policies. Sends a notification if they match. TODO needs " +
      "clarification")
  public Response evaluate(@PathParam("customer") String customer, Map<String,Object> facts) {
    PolicyRetriever policyRetriever = new PolicyRetriever();
    List<Policy> policies = policyRetriever.getEnabledPoliciesForAccount(customer);

    List<String> replies =new ArrayList<>();
    boolean isOneMatch = false;

    for (Policy policy : policies) {
      PolicyParser pp = new PolicyParser(policy);
      boolean match = pp.evaluateConditions(facts);

      replies.add("Policy >" + policy.conditions.trim() + "< match: " + match);

      // TODO We should perhaps collect matching policies and then send notifications in batch
      if (match) {
        notifier.doNotify(policy);
        isOneMatch = true;
      }
    }

    if (isOneMatch) {
      return Response.ok(replies).build();
    } else {
      return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }

  }

  @POST
  @Path("/evalsp/{customer}")
  @Operation(summary = "Evaluate the system profile. Sends a notification if there are matches. TODO needs " +
      "clarification")
  @APIResponse(responseCode = "200", description = "The facts match the stored policy")
  @APIResponse(responseCode = "412", description = "Facts are not matching")
  public Response evaluatesp(@PathParam("customer") String customer, SystemProfile profile) {
    PolicyRetriever policyRetriever = new PolicyRetriever();
    List<Policy> policies = policyRetriever.getEnabledPoliciesForAccount(customer);

    List<String> replies =new ArrayList<>();

    boolean isOneMatch = false;

    for (Policy policy : policies) {
      PolicyParser pp = new PolicyParser(policy);
      boolean match = pp.evaluateConditions(profile.getProfile());

      replies.add("Policy >" + policy.conditions.trim() + "< match: " + match);
      if (match) {
        notifier.doNotify(policy);
        isOneMatch = true;
      }
    }
    if (isOneMatch) {
      return Response.ok(replies).build();
    } else {
      return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }
  }

  @GET
  @Path("/eval/{customer}/{host}")
  public Response evaluateSystemProfile(@PathParam("customer") String customer, @PathParam("host") String host) {

    PolicyRetriever policyRetriever = new PolicyRetriever();
    List<Policy> policies = policyRetriever.getEnabledPoliciesForAccount(customer);

    SystemProfile profile = hbiSystemProfile.getSystemProfileForHost(host);

    System.out.println(profile);

    return Response.ok().entity(profile).build();

  }
}
