package com.redhat.cloud.policies.engine.process;

import com.redhat.cloud.policies.engine.TestLifecycleManager;
import com.redhat.cloud.policies.engine.db.StatelessSessionFactory;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.function.Consumer;

import static com.redhat.cloud.policies.engine.process.Receiver.EVENTS_CHANNEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@QuarkusTestResource(TestLifecycleManager.class)
public class ReceiverTest {

    @InjectMock
    PayloadParser payloadParser;

    @InjectSpy
    StatelessSessionFactory statelessSessionFactory;

    @InjectMock
    EventProcessor eventProcessor;

    @Inject
    @Any
    InMemoryConnector inMemoryConnector;

    @Test
    void testPayloadProcessing() throws InterruptedException {
        String payload = "Hello, world!";
        Event event = new Event();

        when(payloadParser.parse(eq(payload))).thenReturn(Optional.of(event));

        inMemoryConnector.source(EVENTS_CHANNEL).send(payload);

        // Let's give SR Reactive Messaging some time to process the payload.
        Thread.sleep(2000L);

        verify(payloadParser, times(1)).parse(eq(payload));
        verify(statelessSessionFactory, times(1)).withSession(any(Consumer.class));
        verify(eventProcessor, times(1)).process(eq(event));
    }
}
