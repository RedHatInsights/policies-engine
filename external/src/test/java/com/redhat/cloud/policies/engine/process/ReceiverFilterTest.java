package com.redhat.cloud.policies.engine.process;

import io.smallrye.metrics.app.CounterImpl;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReceiverFilterTest {

    MockedAlertsService mockedAlertsService;
    Receiver receiver;

    Counter incomingMessagesCount;
    Counter processingErrors;

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
        incomingMessagesCount = new CounterImpl();
        processingErrors = new CounterImpl();

        receiver = new Receiver();
        receiver.alertsService = mockedAlertsService;
        receiver.processingErrors = processingErrors;
        receiver.incomingMessagesCount = incomingMessagesCount;
    }

    @AfterEach
    public void cleanup() {
        mockedAlertsService.clearEvents();
        receiver.processingErrors.inc(receiver.processingErrors.getCount() * -1);
        receiver.incomingMessagesCount.inc(receiver.incomingMessagesCount.getCount() * -1);
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

}
