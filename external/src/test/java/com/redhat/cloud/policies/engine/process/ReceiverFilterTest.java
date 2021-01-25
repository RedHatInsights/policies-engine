package com.redhat.cloud.policies.engine.process;

import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReceiverFilterTest {

    MockedAlertsService mockedAlertsService;
    Receiver receiver;

    Counter incomingMessagesCount;
    Counter processingErrors;
    Counter rejectedCount;
    Counter rejectedCountReporter;
    Counter rejectedCountType;

    @Test
    public void testAlertsServiceIsCalled() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("input/host.json");
        String inputJson = IOUtils.toString(is, StandardCharsets.UTF_8);

        CompletionStage<Void> voidCompletionStage = receiver.processAsync(Message.of(inputJson));
        voidCompletionStage.toCompletableFuture().get();

        assertEquals(1, mockedAlertsService.getPushedEvents().size());
        assertEquals(1, incomingMessagesCount.getCount());
    }

    @BeforeAll
    public void init() {
        mockedAlertsService = new MockedAlertsService();
        incomingMessagesCount = new InternalCounter();
        processingErrors = new InternalCounter();
        rejectedCount = new InternalCounter();
        rejectedCountReporter = new InternalCounter();
        rejectedCountType = new InternalCounter();

        receiver = new Receiver();
        receiver.alertsService = mockedAlertsService;
        receiver.processingErrors = processingErrors;
        receiver.incomingMessagesCount = incomingMessagesCount;
        receiver.rejectedCount = rejectedCount;
        receiver.rejectedCountReporter = rejectedCountReporter;
        receiver.rejectedCountHost = new InternalCounter();
        receiver.rejectedCountId = new InternalCounter();
        receiver.rejectedCountType = rejectedCountType;
    }

    @AfterEach
    public void cleanup() {
        mockedAlertsService.clearEvents();
        receiver.processingErrors.inc(receiver.processingErrors.getCount() * -1);
        receiver.incomingMessagesCount.inc(receiver.incomingMessagesCount.getCount() * -1);
        receiver.rejectedCount.inc(receiver.rejectedCount.getCount() * -1);
        receiver.rejectedCountReporter.inc(receiver.rejectedCountReporter.getCount() * -1);
    }

    @Test
    public void testFilterWithoutInsightsId() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("input/host.json");
        String inputJson = IOUtils.toString(is, StandardCharsets.UTF_8);

        JsonObject jsonObject = new JsonObject(inputJson);
        jsonObject.getJsonObject("host").remove("insights_id");
        inputJson = jsonObject.toString();

        CompletionStage<Void> voidCompletionStage = receiver.processAsync(Message.of(inputJson));
        voidCompletionStage.toCompletableFuture().get();

        assertEquals(0, mockedAlertsService.getPushedEvents().size());
        assertEquals(1, incomingMessagesCount.getCount());
        assertEquals(0, processingErrors.getCount());
    }

    @Test
    public void testInventoryIdIsStored() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("input/host.json");
        String inputJson = IOUtils.toString(is, StandardCharsets.UTF_8);

        CompletionStage<Void> voidCompletionStage = receiver.processAsync(Message.of(inputJson));
        voidCompletionStage.toCompletableFuture().get();

        assertEquals("ba11a21a-8b22-431b-9b4b-b06006472d54", mockedAlertsService.getPushedEvents().get(0).getTags().get(Receiver.INVENTORY_ID_FIELD).iterator().next());
    }

    @Test
    public void testRejectionTypes() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("input/thomas-host.json");
        String inputJson = IOUtils.toString(is, StandardCharsets.UTF_8);

        JsonObject jsonObject = new JsonObject(inputJson);
        jsonObject.put("type", "deleted");
        inputJson = jsonObject.toString();

        CompletionStage<Void> voidCompletionStage = receiver.processAsync(Message.of(inputJson));
        voidCompletionStage.toCompletableFuture().get();

        assertEquals(0, mockedAlertsService.getPushedEvents().size());
        assertEquals(1, incomingMessagesCount.getCount());
        assertEquals(0, processingErrors.getCount());
        assertEquals(1, rejectedCount.getCount());
        assertEquals(1, rejectedCountType.getCount());

        jsonObject = new JsonObject(inputJson);
        jsonObject.put("type","created");
        jsonObject.getJsonObject("host").put("reporter", "rhsm-conduit");
        inputJson = jsonObject.toString();

        voidCompletionStage = receiver.processAsync(Message.of(inputJson));
        voidCompletionStage.toCompletableFuture().get();

        assertEquals(0, mockedAlertsService.getPushedEvents().size());
        assertEquals(2, incomingMessagesCount.getCount());
        assertEquals(0, processingErrors.getCount());
        assertEquals(2, rejectedCount.getCount());
        assertEquals(1, rejectedCountReporter.getCount());
        assertEquals(1, rejectedCountType.getCount());
    }


    public static class InternalCounter implements Counter {

        private AtomicLong value = new AtomicLong(0);

        @Override
        public void inc() {
            value.incrementAndGet();
        }

        @Override
        public void inc(long l) {
            value.addAndGet(l);
        }

        @Override
        public long getCount() {
            return value.get();
        }
    }
}
