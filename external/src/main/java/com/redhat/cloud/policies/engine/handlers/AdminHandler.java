package com.redhat.cloud.policies.engine.handlers;

import com.redhat.cloud.policies.engine.handlers.util.ResponseUtil;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.hawkular.alerts.api.doc.DocEndpoint;
import org.hawkular.alerts.api.doc.DocPath;
import org.hawkular.alerts.api.doc.DocResponse;
import org.hawkular.alerts.api.doc.DocResponses;
import org.hawkular.alerts.api.services.StatusService;
import org.hawkular.alerts.engine.impl.ispn.IspnAdminService;
import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;

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

    private static final MsgLogger log = MsgLogging.getMsgLogger(AdminHandler.class);

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
        router.put(path + "/rocksdb/compact").handler(this::rocksOperations);
        router.put(path + "/down").handler(this::setAdminDown);
        router.put(path + "/lucene/clean/event").handler(BodyHandler.create()).handler(this::deleteIndexedEvents);
    }

    private void setAdminDown(RoutingContext routing) {
        LivenessHandler.markAsDown();
        routing.response()
                .setStatusCode(204)
                .end();
        log.info("Admin down handler was invoked");
    }

    void executeCleanup() {
        executorService.submit(() -> {
            try {
                statusService.setAdditionalStatus("operation", "running key cleanup");
                adminService.deleteExpiredKeys();
            } finally {
                blockRunning.lazySet(false);
                statusService.setAdditionalStatus("operation", null);
            }
        });
    }

    void executeManualCompaction() {
        executorService.submit(() -> {
            try {
                statusService.setAdditionalStatus("operation", "running rocksdb compaction");
                adminService.executeRocksDBCompaction();
            } catch (Exception e) {
                log.error("Failed to compact RocksDB",e);
            } finally {
                blockRunning.lazySet(false);
                statusService.setAdditionalStatus("operation", null);
            }
        });
    }

    void executeStatistics() {
        executorService.submit(() -> {
            try {
                statusService.setAdditionalStatus("operation", "parsing key statistics");
                adminService.printDataStatistics();
            } finally {
                blockRunning.lazySet(false);
                statusService.setAdditionalStatus("operation", null);
            }
        });
    }

    @DocPath(method = PUT,
            path = "/cleanup",
            name = "Delete expired / invalid items from the cache.",
            notes = "This will update liveness handler with operation annotation and return immediately.")
    @DocResponses(value = {
            @DocResponse(code = 204, message = "Success, processing results.", response = String.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void cleanupExpiredItems(RoutingContext routing) {
        // Spawn background process to do the cleanup, return immediately.
        // Should it set something to the statusService to notify that it is running?
        if(blockRunning.compareAndSet(false, true)) {
            executeCleanup();
            routing.response()
                    .setStatusCode(204)
                    .end();
        } else {
            ResponseUtil.badRequest(routing, "Blocking RocksDB operation is already running");
        }
    }

    @DocPath(method = GET,
            path = "/stats",
            name = "Get detailed key statistics from Infinispan ",
            notes = "This will update liveness handler with operation annotation and return immediately.")
    @DocResponses(value = {
            @DocResponse(code = 204, message = "Success, processing results.", response = String.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void getKeyStatistics(RoutingContext routing) {
        if(blockRunning.compareAndSet(false, true)) {
            executeStatistics();
            routing.response()
                    .setStatusCode(204)
                    .end();
        } else {
            ResponseUtil.badRequest(routing, "Blocking RocksDB operation is already running");
        }
    }

    @DocPath(method = PUT,
            path = "/rocksdb/compact",
            name = "Do manual compaction for all Infinispan RocksDB stores",
            notes = "This will update liveness handler with operation annotation and return immediately.")
    @DocResponses(value = {
            @DocResponse(code = 204, message = "Success, processing results.", response = String.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void rocksOperations(RoutingContext routing) {
        if(blockRunning.compareAndSet(false, true)) {
            executeManualCompaction();
            routing.response()
                    .setStatusCode(204)
                    .end();
        } else {
            ResponseUtil.badRequest(routing, "Blocking RocksDB operation is already running");
        }
    }

    @DocPath(method = PUT,
            path = "/lucene/clean/event",
            name = "Clean the Lucene event index",
            notes = "This will update liveness handler with operation annotation and return immediately.")
    @DocResponses(value = {
            @DocResponse(code = 204, message = "Success, processing results.", response = String.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void deleteIndexedEvents(RoutingContext routing) {
        if (blockRunning.compareAndSet(false, true)) {
            executorService.submit(() -> {
                // Unless requested otherwise with the request body, the index data of events older than 2 days will be deleted.
                int ageInDays = 2;
                if (routing.getBody() != null) {
                    ageInDays = Integer.valueOf(routing.getBodyAsString());
                }
                try {
                    statusService.setAdditionalStatus("operation", "running Lucene index cleaning");
                    adminService.deleteIndexedEventsBefore(ageInDays);
                } catch (Exception e) {
                    log.error("Failed to clean Lucene 'event' index",e);
                } finally {
                    blockRunning.lazySet(false);
                    statusService.setAdditionalStatus("operation", null);
                }
            });
            routing.response()
                    .setStatusCode(204)
                    .end();
        } else {
            ResponseUtil.badRequest(routing, "Blocking Lucene operation is already running");
        }
    }
}
