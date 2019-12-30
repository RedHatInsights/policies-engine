## Quarkus startup for Custom-Policies Engine

* This module is about external interfaces (Kafka, REST)
  * Also the distribution generation

## Starting instructions

Running Kafka instance is required to process inbound messages through the queue. If none exists, one can start Kafka with ``docker run --rm -e RUNTESTS=0 --net=host lensesio/fast-data-dev``

To start a clean instance of Custom Policies Engine, run in this directory: ``mvn quarkus:dev``

Alternative, create container images from ``src/main/docker/Dockerfile.jvm``

If a debugging UI is required, build it from Hawkular-Alerts and set the path in property ``src/main/resources/application.properties`` by modifying the property ``external.org.hawkular.alerts.ui.path``. The UI will be available from path ``/ui/``

REST-interface will be available by default from port ``8083`` (and ``8080`` in containers).