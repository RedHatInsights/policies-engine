package com.redhat.cloud.policies.engine.handlers;

import com.redhat.cloud.policies.engine.handlers.util.ResponseUtil;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.hawkular.alerts.api.doc.DocEndpoint;
import org.hawkular.alerts.api.doc.DocPath;
import org.hawkular.alerts.api.doc.DocResponse;
import org.hawkular.alerts.api.doc.DocResponses;
import org.hawkular.alerts.api.services.StatusService;
import org.hawkular.alerts.engine.impl.ispn.IspnAdminService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hawkular.alerts.api.doc.DocConstants.GET;
import static org.hawkular.alerts.api.doc.DocConstants.PUT;

@DocEndpoint(value = "/admin", description = "Administrative tools")
@ApplicationScoped
public class AdminHandler {

    @Inject
    IspnAdminService adminService;

    @Inject
    StatusService statusService;

    ExecutorService executorService = Executors.newFixedThreadPool(1);

    AtomicBoolean blockRunning = new AtomicBoolean(false);

    @PostConstruct
    public void init(@Observes Router router) {
        String path = "/admin";
        router.put(path + "/cleanup").handler(this::cleanupExpiredItems);
        router.get(path + "/stats").handler(this::getKeyStatistics);
    }

    void executeCleanup() {
        executorService.submit(() -> {
            try {
                statusService.setAdditionalStatus("expireJob", "running");
                adminService.deleteExpiredKeys();
            } finally {
                blockRunning.lazySet(false);
                statusService.setAdditionalStatus("expireJob", null);
            }
        });
    }

    @DocPath(method = PUT,
            path = "/cleanup",
            name = "Delete expired / invalid items from the cache.",
            notes = "This will update liveness handler with expireJob annotation and return immediately.")
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, processing results.", response = String.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void cleanupExpiredItems(RoutingContext routing) {
        // Spawn background process to do the cleanup, return immediately.
        // Should it set something to the statusService to notify that it is running?
        if(blockRunning.compareAndSet(false, true)) {
            executeCleanup();
            routing.response()
                    .setStatusCode(200)
                    .end();
        } else {
            ResponseUtil.badRequest(routing, "Cleanup is already running");
        }
    }

    @DocPath(method = GET,
            path = "/stats",
            name = "Get detailed key statistics from Infinispan ")
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Statistics as plain text body.", response = String.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void getKeyStatistics(RoutingContext routing) {
        routing.vertx().executeBlocking(future -> {
            String dataStatistics = adminService.getDataStatistics();

            future.complete(dataStatistics);

        }, res -> ResponseUtil.result(routing, res));
    }
}
