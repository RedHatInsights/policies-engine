package com.redhat.cloud.policies.engine.handlers;

import com.redhat.cloud.policies.engine.handlers.util.ResponseUtil;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.kafka.common.requests.ApiError;
import org.hawkular.alerts.api.doc.*;
import org.hawkular.alerts.api.model.export.Definitions;
import org.hawkular.alerts.api.model.export.ImportType;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.commons.log.MsgLogger;
import org.hawkular.commons.log.MsgLogging;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static org.hawkular.alerts.api.doc.DocConstants.POST;
import static org.hawkular.alerts.api.json.JsonUtil.fromJson;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@ApplicationScoped
@DocEndpoint(value = "/import", description = "Import of triggers and actions definitions")
public class ImportHandler {
    private static final MsgLogger log = MsgLogging.getMsgLogger(ImportHandler.class);

    @Inject
    DefinitionsService definitionsService;

    @PostConstruct
    public void init(@Observes Router router) {
        String path = "/hawkular/alerts/import";
        router.route().handler(BodyHandler.create());
        router.post(path + "/:strategy").handler(this::importDefinitions);
    }

    @DocPath(method = POST,
            path = "/{strategy}",
            name = "Import a list of full triggers and action definitions.",
            notes = "Return a list of effectively imported full triggers and action definitions. + \n" +
                    " + \n" +
                    "Import options: + \n" +
                    " + \n" +
                    "DELETE + \n" +
                    "" +
                    " + \n" +
                    "Existing data in the backend is DELETED before the import operation. + \n" +
                    "All <<FullTrigger>> and <<ActionDefinition>> objects defined in the <<Definitions>> parameter " +
                    "are imported. + \n" +
                    " + \n" +
                    "ALL + \n" +
                    " + \n" +
                    "Existing data in the backend is NOT DELETED before the import operation. + \n" +
                    "All <<FullTrigger>> and <<ActionDefinition>> objects defined in the <<Definitions>> parameter " +
                    "are imported. + \n" +
                    "Existing <<FullTrigger>> and <<ActionDefinition>> objects are overwritten with new values " +
                    "passed in the <<Definitions>> parameter." +
                    " + \n" +
                    "NEW + \n" +
                    " + \n" +
                    "Existing data in the backend is NOT DELETED before the import operation. + \n" +
                    "Only NEW <<FullTrigger>> and <<ActionDefinition>> objects defined in the <<Definitions>> " +
                    "parameters are imported. + \n" +
                    "Existing <<FullTrigger>> and <<ActionDefinition>> objects are maintained in the backend. + \n" +
                    " + \n" +
                    "OLD + \n" +
                    "Existing data in the backend is NOT DELETED before the import operation. + \n" +
                    "Only <<FullTrigger>> and <<ActionDefinition>> objects defined in the <<Definitions>> parameter " +
                    "that previously exist in the backend are imported and overwritten. + \n" +
                    "New <<FullTrigger>> and <<ActionDefinition>> objects that don't exist previously in the " +
                    "backend are ignored. + \n" +
                    " + \n")
    @DocParameters(value = {
            @DocParameter(name = "strategy", required = true, path = true,
                    description = "Import strategy.",
                    allowableValues = "DELETE,ALL,NEW,OLD"),
            @DocParameter(required = true, body = true, type = Definitions.class,
                    description = "Collection of full triggers and action definitions to import.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Successfully exported list of full triggers and action definitions.", response = Definitions.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ApiError.class)
    })
    public void importDefinitions(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String json = routing.getBodyAsString();
                    String strategy = routing.request().getParam("strategy");
                    Definitions definitions;
                    try {
                        definitions = fromJson(json, Definitions.class);
                    } catch (Exception e) {
                        log.errorf("Error parsing Definitions json: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.NotFoundException(e.toString());
                    }
                    try {
                        ImportType importType = ImportType.valueOf(strategy.toUpperCase());
                        Definitions imported = definitionsService.importDefinitions(tenantId, definitions, importType);
                        future.complete(imported);
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException(e.toString());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }
}
