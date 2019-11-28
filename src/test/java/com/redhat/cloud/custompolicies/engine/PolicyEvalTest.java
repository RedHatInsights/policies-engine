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
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Assert;
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

  // Taken from Quarkus test suite after help from Gunnar Morling
  static KafkaConsumer<String, String> createConsumer(String topic) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Collections.singletonList(topic));
    return consumer;
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
    // supply matching facts
    Map<String, Object> facts = new HashMap<>();
    String[] flags = {"fpu","8bit","foo","cat"};
    facts.put("flags",flags);
    facts.put("os_release","7.5");
    facts.put("arch","x86_64");

    KafkaConsumer<String, String> consumer = createConsumer("notification");

    given()
        .body(facts)
        .contentType(ContentType.JSON)
        .when().post("/api/v1/eval/1")
        .then()
        .statusCode(200);

    // We should now have a notification in Kafka
    ConsumerRecord<String, String> records = consumer.poll(Duration.ofMillis(10000)).iterator().next();
    String val = records.value();
    Assert.assertTrue("Expected value not found", val.startsWith("NOTIFY; EMAIL foo@acme.org"));
  }

  @Test
  void testEvalSystemProfile() {

    File file = new File(".");
    System.err.println(file.getAbsolutePath());
    file = new File("src/test/resources/system_profile_sample.json");
    System.err.println(file.getAbsolutePath());

    given()
        .body(file)
        .contentType(ContentType.JSON)
        .when().post("/api/v1/evalsp/1")
        .then()
        .statusCode(200);

    // TODO check for notification in Kafka. But this may clash with the
    // test1Match() test's receiver.
  }


}
