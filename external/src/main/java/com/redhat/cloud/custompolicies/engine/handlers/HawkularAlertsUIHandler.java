/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
