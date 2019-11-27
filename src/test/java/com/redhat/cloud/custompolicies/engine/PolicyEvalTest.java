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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * @author hrupp
 */
@QuarkusTest
class PolicyEvalTest {

  @ClassRule
  private static PostgreSQLContainer postgreSQLContainer =
      new PostgreSQLContainer("postgres");

  @ClassRule
  private static KafkaContainer kafka = new KafkaContainer();

  @BeforeAll
  static void configurePostgres() throws SQLException, LiquibaseException {
    postgreSQLContainer.start();
    // Now that postgres is started, we need to get its URL and tell Quarkus
    System.err.println("JDBC URL :" + postgreSQLContainer.getJdbcUrl());
    System.setProperty("quarkus.datasource.url", postgreSQLContainer.getJdbcUrl());
    System.setProperty("quarkus.datasource.username","test");
    System.setProperty("quarkus.datasource.password","test");

    PGSimpleDataSource ds = new PGSimpleDataSource();

    // Datasource initialization
    ds.setUrl(postgreSQLContainer.getJdbcUrl());
    ds.setUser(postgreSQLContainer.getUsername());
    ds.setPassword(postgreSQLContainer.getPassword());

    DatabaseConnection dbconn = new JdbcConnection(ds.getConnection());
    ResourceAccessor ra = new FileSystemResourceAccessor("src/test/sql");
    Liquibase liquibase = new Liquibase("dbinit.sql", ra, dbconn);
    liquibase.dropAll();
    liquibase.update(new Contexts());

    // Start Kafka
    kafka.start();
    // It is running, so pass he bootstrap server location to Quarkus
    String kafkaBootstrap =  kafka.getBootstrapServers();
    System.setProperty("kafka.bootstrap.servers", kafkaBootstrap);
  }


  @AfterAll
  static void closePostgres() {
    postgreSQLContainer.stop();
  }

  @Test
  void test1NoMatch() {

    // Policies are already in the DB

    Map<String, Object> facts = new HashMap<>();
    String[] flags = {"fpu","8bit","foo","cat"};
    facts.put("flags",flags);

    given()
        .body(facts)
        .contentType(ContentType.JSON)
        .when().post("/api/v1/eval/1")
        .then()
        .statusCode(412);
  }

  @Test
  void test1Match() {

    // Policies are already in the DB

    Map<String, Object> facts = new HashMap<>();
    String[] flags = {"fpu","8bit","foo","cat"};
    facts.put("flags",flags);
    facts.put("os_version","7.5");
    facts.put("arch","x86_64");

    given()
        .body(facts)
        .contentType(ContentType.JSON)
        .when().post("/api/v1/eval/1")
        .then()
        .statusCode(200);

    // We should now have a notification in Kafka
    // TODO implement
  }
}
