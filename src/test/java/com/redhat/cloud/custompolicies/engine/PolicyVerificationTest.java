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

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.io.File;
import org.junit.jupiter.api.Test;

/**
 * Rest-Api tests for policy verification
 * @author hrupp
 */
@QuarkusTest
class PolicyVerificationTest {

  @Test
  void testSimpleFail() {
    String msg = "IF fail";
    given().body(msg)
        .contentType(ContentType.TEXT)
        .when().post("/api/v1/verify")
        .then()
        .statusCode(400);
  }

  @Test
  void testBasePolicy1() {
    File file = new File("src/test/resources/bad-policy.json");

    given()
        .contentType(ContentType.JSON)
        .body(file)
        .when().post("/api/v1/verifyPolicy")
        .then()
        .statusCode(412);
  }

  @Test
  void testBasePolicy2() {
    File file = new File("src/test/resources/bad-policy2.json");

    given()
        .contentType(ContentType.JSON)
        .body(file)
        .when().post("/api/v1/verifyPolicy")
        .then()
        .statusCode(412);
  }

  @Test
  void testBasePolicy3() {
    File file = new File("src/test/resources/policy.json");

    given()
        .contentType(ContentType.JSON)
        .body(file)
        .when().post("/api/v1/verifyPolicy")
        .then()
        .statusCode(204);
  }

}
