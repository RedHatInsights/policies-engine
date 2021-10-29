## Quarkus startup for Policies Engine

* This module is about external interfaces (Kafka, REST)
  * Also the distribution generation

## Starting instructions

Running Kafka instance is required to process inbound messages through the queue. If none exists, one can start Kafka with ``docker run --rm -e RUNTESTS=0 --net=host lensesio/fast-data-dev``

To start a clean instance of Policies Engine, run in this directory: ``./mvnw quarkus:dev``

If a debugging UI is required, build it from Hawkular-Alerts and set the path in property ``src/main/resources/application.properties`` by modifying the property ``external.org.hawkular.alerts.ui.path``. The UI will be available from path ``/ui/``

REST-interface will be available by default from port ``8083`` (and ``8080`` in containers).

## OpenAPI 3.0 specification

OpenAPI 3.0 specification is available at ``localhost:8083/api/custom-policies/v1.0/openapi.json``

To update the specification, run the Maven build with ``-Pdocgen`` to regenerate the ``openapi.json`` 