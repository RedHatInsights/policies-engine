## Quarkus startup for Custom-Policies Engine

* This module is about external interfaces (Kafka, REST)
  * Also the distribution module
* Old Hawkular-Alerts UI is provided, but no longer generated from the repo (debugging use only)

## Starting instructions

Start Kafka with ``docker run --rm -e RUNTESTS=0 --net=host lensesio/fast-data-dev``
Start clean instance of this with: ``mvn quarkus:dev``

Alternative, create container images from ``src/main/docker/Dockerfile.jvm``