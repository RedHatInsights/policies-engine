package com.redhat.cloud.custompolicies.engine.handlers;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.Optional;

@ApplicationScoped
public class HawkularAlertsUIHandler {
    private static final Logger log = Logger.getLogger(HawkularAlertsUIHandler.class);

    @ConfigProperty(name = "external.org.hawkular.alerts.ui.path")
    Optional<String> staticFilesPath;

    @PostConstruct
    public void init(@Observes Router router) {
        if(staticFilesPath.isPresent()) {
            log.infof("Serving Hawkular-Alerts UI from %s", staticFilesPath.get());
            router.route("/ui/*").handler(StaticHandler.create().setAllowRootFileSystemAccess(true).setCachingEnabled(false).setWebRoot(staticFilesPath.get()));
        }
    }
}
