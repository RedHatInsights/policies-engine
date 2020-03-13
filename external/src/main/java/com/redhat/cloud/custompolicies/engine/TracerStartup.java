package com.redhat.cloud.custompolicies.engine;

import io.opentracing.Tracer;
import io.opentracing.contrib.vertx.ext.web.TracingHandler;
import io.vertx.ext.web.Router;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Set up distributed tracing
 * @author hrupp
 */
@ApplicationScoped
public class TracerStartup {

    @Inject
    Tracer tracer;

    @PostConstruct
    public void init(@Observes Router router) {

        TracingHandler handler = new TracingHandler(tracer);
        router.route()
            .order(-1).handler(handler)
            .failureHandler(handler);
    }
}
