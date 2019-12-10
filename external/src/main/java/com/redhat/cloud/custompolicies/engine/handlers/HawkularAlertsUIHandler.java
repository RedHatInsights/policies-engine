package com.redhat.cloud.custompolicies.engine.handlers;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.Optional;

@ApplicationScoped
public class HawkularAlertsUIHandler {
    @ConfigProperty(name = "com.redhat.cloud.custompolicies.engine.org.hawkular.alerts.ui.path")
    Optional<String> staticFilesPath;

    @PostConstruct
    public void init(@Observes Router router) {
        if(staticFilesPath.isPresent()) {
            System.out.println("Creating static path");
            router.route("/ui/*").handler(StaticHandler.create().setAllowRootFileSystemAccess(true).setCachingEnabled(false).setWebRoot(staticFilesPath.get()));
        }
    }
}
