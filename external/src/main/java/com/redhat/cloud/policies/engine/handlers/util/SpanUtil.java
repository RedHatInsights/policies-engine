package com.redhat.cloud.policies.engine.handlers.util;

import io.opentracing.Span;
import io.vertx.ext.web.RoutingContext;

/**
 * @author hrupp
 */
public abstract class SpanUtil {

    public static Span getServerSpan(RoutingContext routing) {
        // 'sever' is correct here. Changing this to 'server' will return no data
        return routing.get("io.opentracing.contrib.vertx.ext.web.TracingHandler.severSpan");
    }

}
