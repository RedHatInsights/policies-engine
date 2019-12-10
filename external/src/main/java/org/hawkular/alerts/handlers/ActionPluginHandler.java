/*
 * Copyright 2015-2017 Red Hat, Inc. and/or its affiliates
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
package org.hawkular.alerts.handlers;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.hawkular.alerts.actions.QuarkusActionPluginRegister;
import org.hawkular.alerts.api.doc.*;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.handlers.util.ResponseUtil;
import org.hawkular.alerts.handlers.util.ResponseUtil.ApiError;
import org.hawkular.commons.log.MsgLogger;
import org.hawkular.commons.log.MsgLogging;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Set;

import static org.hawkular.alerts.api.doc.DocConstants.GET;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocEndpoint(value = "/plugins", description = "Query operations for action plugins")
public class ActionPluginHandler {
    private static final MsgLogger log = MsgLogging.getMsgLogger(ActionPluginHandler.class);

    @Inject
    DefinitionsService definitionsService;

    @Inject
    QuarkusActionPluginRegister pluginRegister;

    @PostConstruct
    public void init(@Observes Router router) {
        String path = "/hawkular/alerts/plugins";
        router.get(path).handler(this::findActionPlugins);
        router.get(path + "/:actionPlugin").handler(this::getActionPlugin);
    }

    @DocPath(method = GET,
            path = "/",
            name = "Find all action plugins.")
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Successfully fetched list of actions plugins.", response = String.class, responseContainer = "List"),
            @DocResponse(code = 500, message = "Internal server error.", response = ApiError.class)
    })
    public void findActionPlugins(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                   ResponseUtil.checkTenant(routing);
                   try {
                       Collection<String> actionPlugins = definitionsService.getActionPlugins();
                       log.debugf("ActionPlugins: %s", actionPlugins);
                        future.complete(actionPlugins);
                   } catch (Exception e) {
                       log.errorf("Error querying all plugins. Reason: %s", e.toString());
                       throw new ResponseUtil.InternalServerException(e.toString());
                   }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = GET,
            path = "/{actionPlugin}",
            name = "Find list of properties to fill for a specific action plugin.",
            notes = "Each action plugin can have a different and variable number of properties. + \n" +
                    "This method should be invoked before of a creation of a new action.")
    @DocParameters(value = {
            @DocParameter(name = "actionPlugin", required = true, path = true,
                    description = "Action plugin to query.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Action Plugin found.", response = String.class, responseContainer = "List"),
            @DocResponse(code = 404, message = "Action Plugin not found.", response = ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ApiError.class)
    })
    public void getActionPlugin(RoutingContext routing) {
        String actionPlugin = routing.request().getParam("actionPlugin");
        routing.vertx()
                .executeBlocking(future -> {
                    ResponseUtil.checkTenant(routing);
                    Set<String> actionPluginProps;
                    try {
                        actionPluginProps = definitionsService.getActionPlugin(actionPlugin);
                        log.debugf("ActionPlugin: %s - Properties: %s", actionPlugin, actionPluginProps);
                        if (actionPluginProps == null) {
                            future.fail(new ResponseUtil.NotFoundException("Not found action plugin: " + actionPlugin));
                        } else {
                            future.complete(actionPluginProps);
                        }
                    } catch (Exception e) {
                        log.errorf("Error querying plugin %s. Reason: %s", actionPlugin, e.toString());
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }
}
