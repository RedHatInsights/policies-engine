FROM registry.access.redhat.com/ubi7-minimal

LABEL maintainer="hrupp@redhat.com"
RUN microdnf install java-1.8.0-openjdk-headless && microdnf clean all

ENV quarkus.http.port=8083 \
    kafka.bootstrap.servers=localhost:9092 \
    quarkus.datasouce.url=jdbc:postgresql://localhost:5432/postgres \
    quarkus.datasource.username=sarah \
    quarkus.datasource.password=connor

EXPOSE 8083

COPY lib /lib
COPY engine-1.0-SNAPSHOT-runner.jar /

CMD "java" "-jar" "engine-1.0-SNAPSHOT-runner.jar"

