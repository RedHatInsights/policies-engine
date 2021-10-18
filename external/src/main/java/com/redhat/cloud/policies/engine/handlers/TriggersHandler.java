package com.redhat.cloud.policies.engine.handlers;

import com.redhat.cloud.policies.engine.handlers.util.ResponseUtil;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.hawkular.alerts.api.doc.*;
import org.hawkular.alerts.api.exception.NotFoundException;
import org.hawkular.alerts.api.json.GroupConditionsInfo;
import org.hawkular.alerts.api.json.GroupMemberInfo;
import org.hawkular.alerts.api.json.UnorphanMemberInfo;
import org.hawkular.alerts.api.model.Note;
import org.hawkular.alerts.api.model.condition.Condition;
import org.hawkular.alerts.api.model.dampening.Dampening;
import org.hawkular.alerts.api.model.paging.Page;
import org.hawkular.alerts.api.model.paging.Pager;
import org.hawkular.alerts.api.model.trigger.FullTrigger;
import org.hawkular.alerts.api.model.trigger.Mode;
import org.hawkular.alerts.api.model.trigger.Trigger;
import org.hawkular.alerts.api.services.DefinitionsService;
import org.hawkular.alerts.api.services.TriggersCriteria;
import org.hawkular.alerts.log.MsgLogger;
import org.hawkular.alerts.log.MsgLogging;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.redhat.cloud.policies.engine.handlers.util.SpanUtil.getServerSpan;
import static org.hawkular.alerts.api.doc.DocConstants.*;
import static org.hawkular.alerts.api.json.JsonUtil.collectionFromJson;
import static org.hawkular.alerts.api.json.JsonUtil.fromJson;
import static org.hawkular.alerts.api.util.Util.isEmpty;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
@DocEndpoint(value = "/triggers", description = "Triggers Definitions Handling")
@ApplicationScoped
public class TriggersHandler {
    private static final MsgLogger log = MsgLogging.getMsgLogger(TriggersHandler.class);
    private static final String PARAM_KEEP_NON_ORPHANS = "keepNonOrphans";
    private static final String PARAM_KEEP_ORPHANS = "keepOrphans";
    private static final String PARAM_INCLUDE_ORPHANS = "includeOrphans";
    private static final String PARAM_TRIGGER_IDS = "triggerIds";
    private static final String PARAM_THIN = "thin";
    private static final String PARAM_QUERY = "query";
    private static final String PARAM_ENABLED = "enabled";

    private static final String FIND_TRIGGERS = "findTriggers";
    private static final Map<String, Set<String>> queryParamValidationMap = new HashMap<>();
    static {
        Collection<String> TRIGGERS_CRITERIA = Arrays.asList(
                PARAM_TRIGGER_IDS,
                PARAM_QUERY,
                PARAM_THIN);
        queryParamValidationMap.put(FIND_TRIGGERS, new HashSet<>(TRIGGERS_CRITERIA));
        queryParamValidationMap.get(FIND_TRIGGERS).addAll(Arrays.asList(ResponseUtil.PARAMS_PAGING));
    }

    @Inject
    DefinitionsService definitionsService;

    @Inject
    Tracer tracer;

    @PostConstruct
    public void init(@Observes Router router) {
        String path = "/hawkular/alerts/triggers";
        router.get(path).handler(this::findTriggers);
        router.post(path).handler(BodyHandler.create()).handler(this::createTrigger);
        router.get(path + "/:triggerId").handler(this::getTrigger);
        router.post(path + "/trigger").handler(BodyHandler.create()).handler(this::createFullTrigger);
        router.put(path + "/trigger/:triggerId").handler(BodyHandler.create()).handler(this::updateFullTrigger);
        router.post(path + "/groups").handler(BodyHandler.create()).handler(this::createGroupTrigger);
        router.put(path + "/enabled").handler(this::setTriggersEnabled);
        router.put(path + "/:triggerId").handler(BodyHandler.create()).handler(this::updateTrigger);
        router.put(path + "/:triggerId/enable").handler(this::setTriggerEnabled);
        router.delete(path + "/:triggerId/enable").handler(this::setTriggerDisabled);
        router.delete(path + "/:triggerId").handler(this::deleteTrigger);
        router.get(path + "/trigger/:triggerId").handler(this::getFullTrigger);
        router.get(path + "/:triggerId/dampenings").handler(this::getTriggerDampenings);
        router.get(path + "/:triggerId/conditions").handler(this::getTriggerConditions);
        router.post(path + "/groups/members").handler(BodyHandler.create()).handler(this::createGroupMember);
        router.post(path + "/:triggerId/dampenings").handler(BodyHandler.create()).handler(this::createDampening);
        router.put(path + "/groups/enabled").handler(this::setGroupTriggersEnabled);
        router.put(path + "/groups/:groupId").handler(BodyHandler.create()).handler(this::updateGroupTrigger);
        router.put(path + "/:triggerId/conditions").handler(BodyHandler.create()).handler(this::setAllConditions);
        router.delete(path + "/groups/:groupId").handler(this::deleteGroupTrigger);
        router.get(path + "/:triggerId/dampenings/:dampeningId").handler(this::getDampening);
        router.get(path + "/groups/:groupId/members").handler(this::findGroupMembers);
        router.post(path + "/groups/:groupId/dampenings").handler(BodyHandler.create()).handler(this::createGroupDampening);
        router.put(path + "/:triggerId/dampenings/:dampeningId").handler(BodyHandler.create()).handler(this::updateDampening);
        router.put(path + "/:triggerId/conditions/:triggerMode").handler(BodyHandler.create()).handler(this::setConditions);
        router.put(path + "/groups/:groupId/conditions").handler(BodyHandler.create()).handler(this::setGroupConditions);
        router.delete(path + "/:triggerId/dampenings/:dampeningId").handler(this::deleteDampening);
        router.get(path + "/:triggerId/dampenings/mode/:triggerMode").handler(this::getTriggerModeDampenings);
        router.put(path + "/groups/members/:memberId/orphan").handler(this::orphanMemberTrigger);
        router.put(path + "/groups/members/:memberId/unorphan").handler(BodyHandler.create()).handler(this::unorphanMemberTrigger);
        router.put(path + "/groups/:groupId/dampenings/:dampeningId").handler(BodyHandler.create()).handler(this::updateGroupDampening);
        router.put(path + "/groups/:groupId/conditions/:triggerMode").handler(BodyHandler.create()).handler(this::setGroupConditionsTriggerMode);
        router.delete(path + "/groups/:groupId/dampenings/:dampeningId").handler(this::deleteGroupDampening);
    }

    @DocPath(method = POST,
            path = "/{triggerId}/dampenings",
            name = "Create a new dampening.",
            notes = "Return Dampening created.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id attached to dampening."),
            @DocParameter(required = true, body = true, type = Dampening.class,
                    description = "Dampening definition to be created.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Dampening created.", response = Dampening.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void createDampening(RoutingContext routing) {
        createDampening(routing, false);
    }

    @DocPath(method = POST,
            path = "/groups/{groupId}/dampenings",
            name = "Create a new group dampening.",
            notes = "Return group Dampening created.")
    @DocParameters(value = {
            @DocParameter(name = "groupId", required = true, path = true,
                    description = "Group Trigger definition id attached to dampening."),
            @DocParameter(required = true, body = true, type = Dampening.class,
                    description = "Dampening definition to be created.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Dampening created.", response = Dampening.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void createGroupDampening(RoutingContext routing) {
        createDampening(routing, true);
    }

    void createDampening(RoutingContext routing, boolean isGroup) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String json = routing.getBodyAsString();
                    String triggerId = routing.request().getParam("triggerId");
                    String groupId = routing.request().getParam("groupId");
                    Dampening dampening;
                    try {
                        dampening = fromJson(json, Dampening.class);
                    } catch (Exception e) {
                        log.debugf("Error parsing Dampening json: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e.toString());
                    }
                    dampening.setTenantId(tenantId);
                    dampening.setTriggerId(isGroup ? groupId : triggerId);
                    Dampening found = null;
                    try {
                        found = definitionsService.getDampening(tenantId, dampening.getDampeningId());
                    } catch(NotFoundException e) {
                        // Expected
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                    if (found != null) {
                        throw new ResponseUtil.BadRequestException("Existing dampening for dampeningId: " + dampening.getDampeningId());
                    }
                    try {
                        Dampening d = ResponseUtil.getCleanDampening(dampening);
                        if (isGroup) {
                            definitionsService.addGroupDampening(tenantId, d);
                        } else {
                            definitionsService.addDampening(tenantId, d);
                        }
                        log.debugf("Dampening: %s", dampening);
                        future.complete(d);
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = POST,
            path = "/trigger",
            name = "Create a new full trigger (trigger, dampenings and conditions).",
            notes = "Return created full trigger.")
    @DocParameters(value = {
            @DocParameter(required = true, body = true, type = FullTrigger.class,
                    description = "FullTrigger (trigger, dampenings, conditions) to be created."),
            @DocParameter(name = "dry", type = Boolean.class, description = "Process the trigger (validate), but do not store it."),
            @DocParameter(name = "user", description = "User who created the trigger"),
            @DocParameter(name = "text", description = "Additional description for the note")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, FullTrigger created.", response = FullTrigger.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    @Timed(absolute = true, tags = {"path=/triggers/trigger", "method=POST"})
    public void createFullTrigger(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String json = routing.getBodyAsString();
                    Span serverSpan = getServerSpan(routing);
                    Boolean dryRun = Boolean.valueOf(routing.request().getParam("dry"));
                    FullTrigger fullTrigger;
                    try {
                        fullTrigger = fromJson(json, FullTrigger.class);
                    } catch (Exception e) {
                        log.debugf("Error parsing FullTrigger json: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e.getMessage());
                    }
                    if (fullTrigger.getTrigger() == null) {
                        throw new ResponseUtil.BadRequestException("Trigger is empty");
                    }

                    try {
                        for (Condition condition : fullTrigger.getConditions()) {
                            condition.validate();
                        }
                    } catch(Exception e) {
                        log.debugf("Error validating condition from json request: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e.getMessage());
                    }

                    Trigger trigger = fullTrigger.getTrigger();
                    trigger.setTenantId(tenantId);
                    Note note = parseNotes(routing);
                    trigger.addLifecycle(Trigger.TriggerLifecycle.CREATED, 0, note);
                    if (isEmpty(trigger.getId())) {
                        trigger.setId(Trigger.generateId());
                    } else {
                        Trigger found = null;
                        Span span = tracer.buildSpan("getTrigger").asChildOf(serverSpan).start();
                        try (Scope ignored = tracer.scopeManager().activate(span)) {
                            found = definitionsService.getTrigger(tenantId, trigger.getId());
                        } catch (NotFoundException e) {
                            // Expected
                        } catch (Exception e) {
                            log.debug(e.getMessage(), e);
                            throw new ResponseUtil.InternalServerException(e.toString());
                        } finally {
                            span.finish();
                        }
                        if (found != null) {
                            throw new ResponseUtil.BadRequestException("Trigger with ID [" + trigger.getId() + "] exists.");
                        }
                    }
                    if (!ResponseUtil.checkTags(trigger)) {
                        throw new ResponseUtil.BadRequestException("Tags " + trigger.getTags() + " must be non empty.");
                    }
                    Span span = tracer.buildSpan("createFullTrigger").asChildOf(serverSpan).start();
                    try (Scope ignored = tracer.scopeManager().activate(span)) {
                        span.setTag("dryRun",dryRun);
                        if(!dryRun) {
                            definitionsService.createFullTrigger(tenantId, fullTrigger);
                        }
                        future.complete(fullTrigger);

                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e);
                    } finally {
                        span.finish();
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = PUT,
            path = "/trigger/{triggerId}",
            name = "Update an existing full trigger (trigger, dampenings and conditions).",
            notes = "Return updated full trigger.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be updated."),
            @DocParameter(required = true, body = true, type = FullTrigger.class,
                    description = "FullTrigger (trigger, dampenings, conditions) to be created."),
            @DocParameter(name = "dry", type = Boolean.class, description = "Process the trigger (validate), but do not store it."),
            @DocParameter(name = "user", description = "User who modified the trigger"),
            @DocParameter(name = "text", description = "Additional description for the note")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, FullTrigger updated.", response = FullTrigger.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    @Timed(absolute = true, tags = {"path=/triggers/trigger/{triggerId}", "method=PUT"})
    public void updateFullTrigger(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String json = routing.getBodyAsString();
                    String triggerId = routing.request().getParam("triggerId");
                    Boolean dryRun = Boolean.valueOf(routing.request().getParam("dry"));
                    FullTrigger fullTrigger;
                    Trigger trigger;
                    try {
                        fullTrigger = fromJson(json, FullTrigger.class);
                    } catch (Exception e) {
                        log.debugf("Error parsing FullTrigger json: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e.getMessage());
                    }
                    if (null == fullTrigger) {
                        throw new ResponseUtil.BadRequestException("FullTrigger can not be null.");
                    }
                    try {
                        for (Condition condition : fullTrigger.getConditions()) {
                            condition.validate();
                        }
                    } catch(Exception e) {
                        log.debugf("Error validating condition from json request: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e.getMessage());
                    }
                    trigger = fullTrigger.getTrigger();
                    if (null == trigger) {
                        throw new ResponseUtil.BadRequestException("FullTrigger.Trigger can not be null.");
                    }
                    trigger.getLifecycle().clear();
                    trigger.addLifecycle(Trigger.TriggerLifecycle.MODIFIED, 0, parseNotes(routing));
                    trigger.setId(triggerId);
                    if (!ResponseUtil.checkTags(trigger)) {
                        throw new ResponseUtil.BadRequestException(
                                "Tags " + trigger.getTags() + " must be non empty.");
                    }
                    try {
                        if(!dryRun) {
                            definitionsService.updateFullTrigger(tenantId, fullTrigger);
                        }
                        log.debugf("FullTrigger: %s", fullTrigger);
                        future.complete(fullTrigger);
                    } catch (NotFoundException e) {
                        throw new NotFoundException(e.getMessage());
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = POST,
            path = "/groups/members",
            name = "Create a new member trigger for a parent trigger.",
            notes = "Returns Member Trigger created if operation finished correctly.")
    @DocParameters(value = {
            @DocParameter(required = true, body = true, type = GroupMemberInfo.class,
                    description = "Group member trigger to be created.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Member Trigger Created.", response = Trigger.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "Group trigger not found.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void createGroupMember(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String json = routing.getBodyAsString();
                    GroupMemberInfo groupMember;
                    try {
                        groupMember = fromJson(json, GroupMemberInfo.class);
                    } catch (Exception e) {
                        log.debugf("Error parsing GroupMemberInfo json: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e.toString());
                    }
                    String groupId = groupMember.getGroupId();
                    if (isEmpty(groupId)) {
                        throw new ResponseUtil.BadRequestException("MemberTrigger groupId is null");
                    }
                    if (!ResponseUtil.checkTags(groupMember)) {
                        throw new ResponseUtil.BadRequestException("Tags " + groupMember.getMemberTags() + " must be non empty.");
                    }
                    try {
                        Trigger child = definitionsService.addMemberTrigger(tenantId, groupId, groupMember.getMemberId(),
                                groupMember.getMemberName(),
                                groupMember.getMemberDescription(),
                                groupMember.getMemberContext(),
                                groupMember.getMemberTags(),
                                groupMember.getDataIdMap());
                        log.debugf("Child Trigger: %s", child);
                        future.complete(child);
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = POST,
            path = "/",
            name = "Create a new trigger.",
            notes = "Returns created trigger.")
    @DocParameters(value = {
            @DocParameter(required = true, body = true, type = Trigger.class,
                    description = "Trigger definition to be created.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Trigger Created.", response = Trigger.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void createTrigger(RoutingContext routing) {
        createTrigger(routing, false);
    }

    @DocPath(method = POST,
            path = "/groups",
            name = "Create a new group trigger.",
            notes = "Returns created group trigger.")
    @DocParameters(value = {
            @DocParameter(required = true, body = true, type = Trigger.class,
                    description = "Group member trigger to be created."),
            @DocParameter(name = "user", description = "User who created the trigger"),
            @DocParameter(name = "text", description = "Additional description for the note")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Group Trigger Created.", response = Trigger.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void createGroupTrigger(RoutingContext routing) {
        createTrigger(routing, true);
    }

    void createTrigger(RoutingContext routing, boolean isGroup) {
        routing.vertx()
                .executeBlocking(future -> {
                    Span serverSpan = getServerSpan(routing);
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String json = routing.getBodyAsString();
                    Trigger trigger;
                    try {
                        trigger = fromJson(json, Trigger.class);
                    } catch (Exception e) {
                        log.debugf("Error parsing Trigger json: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e);
                    }
                    if (isEmpty(trigger.getId())) {
                        trigger.setId(Trigger.generateId());
                    } else {
                        Trigger found = null;
                        Span span = tracer.buildSpan("getTrigger").asChildOf(serverSpan).start();
                        try (Scope ignored = tracer.scopeManager().activate(span)) {
                            found = definitionsService.getTrigger(tenantId, trigger.getId());
                        } catch (NotFoundException e) {
                            // expected
                        } catch (Exception e) {
                            log.debug(e.getMessage(), e);
                            throw new ResponseUtil.InternalServerException(e.toString());
                        } finally {
                            span.finish();
                        }
                        if (found != null) {
                            throw new ResponseUtil.BadRequestException("Trigger with ID [" + trigger.getId() + "] exists.");
                        }
                    }
                    trigger.addLifecycle(Trigger.TriggerLifecycle.CREATED, 0, parseNotes(routing));
                    if (!ResponseUtil.checkTags(trigger)) {
                        throw new ResponseUtil.BadRequestException("Tags " + trigger.getTags() + " must be non empty.");
                    }
                    Span span = tracer.buildSpan("addNewTrigger").asChildOf(serverSpan).start();
                    try (Scope ignored = tracer.scopeManager().activate(span)) {
                        if (isGroup) {
                            definitionsService.addGroupTrigger(tenantId, trigger);
                        } else {
                            definitionsService.addTrigger(tenantId, trigger);
                        }
                        log.debugf("Trigger: %s", trigger);
                        future.complete(trigger);
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    } finally {
                        span.finish();
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = DELETE,
            path = "/{triggerId}/dampenings/{dampeningId}",
            name = "Delete an existing dampening definition.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be deleted."),
            @DocParameter(name = "dampeningId", required = true, path = true,
                    description = "Dampening id for dampening definition to be deleted.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Dampening updated.", response = FullTrigger.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void deleteDampening(RoutingContext routing) {
        deleteDampening(routing, false);
    }

    @DocPath(method = DELETE,
            path = "/groups/{groupId}/dampenings/{dampeningId}",
            name = "Delete an existing group dampening definition.")
    @DocParameters(value = {
            @DocParameter(name = "groupId", required = true, path = true,
                    description = "Group Trigger definition id to be deleted."),
            @DocParameter(name = "dampeningId", required = true, path = true,
                    description = "Dampening id for dampening definition to be deleted.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Dampening updated.", response = FullTrigger.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void deleteGroupDampening(RoutingContext routing) {
        deleteDampening(routing, true);
    }

    void deleteDampening(RoutingContext routing, boolean isGroup) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String dampeningId = routing.request().getParam("dampeningId");
                    Dampening found;
                    try {
                        found = definitionsService.getDampening(tenantId, dampeningId);
                        if (found != null) {
                            if (isGroup) {
                                definitionsService.removeGroupDampening(tenantId, dampeningId);
                            } else {
                                definitionsService.removeDampening(tenantId, dampeningId);
                            }
                            log.debugf("DampeningId: %s", dampeningId);
                            future.complete(found);
                            return;
                        }
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                    throw new ResponseUtil.NotFoundException("Dampening " + dampeningId + " not found ");
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = DELETE,
            path = "/groups/{groupId}",
            name = "Delete a group trigger.")
    @DocParameters(value = {
            @DocParameter(name = "groupId", required = true, path = true,
                    description = "Group Trigger definition id to be deleted."),
            @DocParameter(name = "keepNonOrphans", required = true, type = Boolean.class,
                    description = "Convert the non-orphan member triggers to standard triggers."),
            @DocParameter(name = "keepOrphans", required = true, type = Boolean.class,
                    description = "Convert the orphan member triggers to standard triggers.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Group Trigger Removed."),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "Group Trigger not found.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void deleteGroupTrigger(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String groupId = routing.request().getParam("groupId");
                    try {
                        boolean keepNonOrphans = false;
                        if (routing.request().params().get(PARAM_KEEP_NON_ORPHANS) != null) {
                            keepNonOrphans = Boolean.valueOf(routing.request().params().get(PARAM_KEEP_NON_ORPHANS));
                        }
                        boolean keepOrphans = false;
                        if (routing.request().params().get(PARAM_KEEP_ORPHANS) != null) {
                            keepOrphans = Boolean.valueOf(routing.request().params().get(PARAM_KEEP_ORPHANS));
                        }
                        definitionsService.removeGroupTrigger(tenantId, groupId, keepNonOrphans, keepOrphans);
                        if (log.isDebugEnabled()) {
                            log.debugf("Remove Group Trigger: %s / %s", tenantId, groupId);
                        }
                        future.complete();
                    } catch (NotFoundException e) {
                        throw new ResponseUtil.NotFoundException(e.getMessage());
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = DELETE,
            path = "/{triggerId}",
            name = "Delete an existing standard or group member trigger definition.",
            notes = "This can not be used to delete a group trigger definition.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be deleted.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Trigger deleted."),
            @DocResponse(code = 404, message = "Trigger not found", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    @Timed(absolute = true, tags = {"path=/triggers/{triggerId}", "method=DELETE"})
    public void deleteTrigger(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String triggerId = routing.request().getParam("triggerId");
                    try {
                        definitionsService.removeTrigger(tenantId, triggerId);
                        log.debugf("TriggerId: %s", triggerId);
                        future.complete();
                    } catch (NotFoundException e) {
                        throw new ResponseUtil.NotFoundException(e.getMessage());
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = GET,
            path = "/groups/{groupId}/members",
            name = "Find all group member trigger definitions.",
            notes = "No pagination.")
    @DocParameters(value = {
            @DocParameter(name = "groupId", required = true, path = true,
                    description = "Group Trigger definition id."),
            @DocParameter(name = "includeOrphans",
                    description = "include Orphan members? No if omitted."),
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Successfully fetched list of triggers.", response = Trigger.class, responseContainer = "List"),
            @DocResponse(code = 404, message = "Trigger not found", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void findGroupMembers(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String groupId = routing.request().getParam("groupId");
                    try {
                        boolean includeOrphans = false;
                        if (routing.request().params().get(PARAM_INCLUDE_ORPHANS) != null) {
                            includeOrphans = Boolean.valueOf(routing.request().params().get(PARAM_INCLUDE_ORPHANS));
                        }
                        Collection<Trigger> members = definitionsService.getMemberTriggers(tenantId, groupId, includeOrphans);
                        log.debugf("Member Triggers: %s", members);
                        future.complete(members);
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = GET,
            path = "/",
            name = "Get triggers with optional filtering.",
            notes = "If not criteria defined, it fetches all triggers stored in the system.")
    @DocParameters(value = {
            @DocParameter(name = "query",
                    description = "Filter out triggers.",
                    allowableValues = "Valid query language statement"),
            @DocParameter(name = "thin", type = Boolean.class,
                    description = "Return only thin triggers. Currently Ignored.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Successfully fetched list of triggers.", response = Trigger.class, responseContainer = "List"),
            @DocResponse(code = 404, message = "Trigger not found", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    @Timed(absolute = true, tags = {"path=/triggers", "method=GET"})
    public void findTriggers(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    Span serverSpan = getServerSpan(routing);
                    String tenantId = ResponseUtil.checkTenant(routing);
                    try {
                        ResponseUtil.checkForUnknownQueryParams(routing.request().params(), queryParamValidationMap.get(FIND_TRIGGERS));
                        Pager pager = ResponseUtil.extractPaging(routing.request().params());
                        TriggersCriteria criteria = buildCriteria(routing.request().params());
                        Page<Trigger> triggerPage;
                        Span span = tracer.buildSpan("getTriggers").asChildOf(serverSpan).start();
                        try (Scope ignored = tracer.scopeManager().activate(span)) {
                            triggerPage = definitionsService.getTriggers(tenantId, criteria, pager);
                        } finally {
                            span.finish();
                        }
                        log.debugf("Triggers: %s", triggerPage);
                        future.complete(triggerPage);
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = GET,
            path = "/{triggerId}/dampenings/{dampeningId}",
            name = "Get an existing dampening.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be retrieved."),
            @DocParameter(name = "dampeningId", required = true, path = true,
                    description = "Dampening id")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Successfully fetched list of triggers.", response = Dampening.class),
            @DocResponse(code = 404, message = "Damppening not found", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void getDampening(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String dampeningId = routing.request().getParam("dampeningId");
                    Dampening found;
                    try {
                        found = definitionsService.getDampening(tenantId, dampeningId);
                        if (found != null) {
                            future.complete(found);
                            return;
                        }
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                    throw new ResponseUtil.NotFoundException("No dampening found for dampeningId:" + dampeningId);
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = GET,
            path = "/{triggerId}",
            name = "Get an existing trigger definition.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be retrieved.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Trigger found.", response = Trigger.class),
            @DocResponse(code = 404, message = "Trigger not found", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void getTrigger(RoutingContext routing) {
        getTrigger(routing, false);
    }

    @DocPath(method = GET,
            path = "/trigger/{triggerId}",
            name = "Get an existing full trigger definition (trigger, dampenings and conditions).")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be retrieved.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Trigger found.", response = FullTrigger.class),
            @DocResponse(code = 404, message = "Trigger not found", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    @Timed(absolute = true, tags = {"path=/triggers/trigger/{triggerId}", "method=GET"})
    public void getFullTrigger(RoutingContext routing) {
        getTrigger(routing, true);
    }

    void getTrigger(RoutingContext routing, boolean isFullTrigger) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String triggerId = routing.request().getParam("triggerId");
                    Object found = null;
                    try {
                        found = isFullTrigger ? definitionsService.getFullTrigger(tenantId, triggerId)
                                : definitionsService.getTrigger(tenantId, triggerId);
                    } catch (NotFoundException e) {
                        // Expected
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                    if (found == null) {
                        throw new ResponseUtil.NotFoundException("triggerId: " + triggerId + " not found");
                    }
                    log.debugf("Trigger: %s", found);
                    future.complete(found);
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = GET,
            path = "/{triggerId}/conditions",
            name = "Get all conditions for a specific trigger.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be retrieved.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Successfully fetched list of conditions.", response = Condition.class, responseContainer = "List"),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void getTriggerConditions(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String triggerId = routing.request().getParam("triggerId");
                    try {
                        Collection<Condition> conditions = definitionsService.getTriggerConditions(tenantId, triggerId, null);
                        log.debugf("Conditions: %s", conditions);
                        future.complete(conditions);
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = GET,
            path = "/{triggerId}/dampenings",
            name = "Get all Dampenings for a Trigger (1 Dampening per mode).")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be retrieved.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Trigger found.", response = Dampening.class, responseContainer = "List"),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void getTriggerDampenings(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String triggerId = routing.request().getParam("triggerId");
                    String triggerMode = routing.request().getParam("triggerMode");
                    Mode mode = null;
                    if (triggerMode != null) {
                        mode = Mode.valueOf(triggerMode);
                    }
                    try {
                        Collection<Dampening> dampenings = definitionsService.getTriggerDampenings(tenantId, triggerId, mode);
                        log.debugf("Dampenings: %s", dampenings);
                        future.complete(dampenings);
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = GET,
            path = "/{triggerId}/dampenings/mode/{triggerMode}",
            name = "Get dampening using triggerId and triggerMode.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be retrieved."),
            @DocParameter(name = "triggerMode", required = true, path = true,
                    description = "Trigger mode.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Trigger found.", response = Dampening.class, responseContainer = "List"),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void getTriggerModeDampenings(RoutingContext routing) {
        getTriggerDampenings(routing);
    }

    @DocPath(method = PUT,
            path = "/{triggerId}",
            name = "Update an existing trigger definition.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be updated."),
            @DocParameter(required = true, body = true, type = Trigger.class,
                    description = "Updated trigger definition."),
            @DocParameter(name = "user", description = "User who created the trigger"),
            @DocParameter(name = "text", description = "Additional description for the note")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Trigger updated.", response = Trigger.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "Trigger not found.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void updateTrigger(RoutingContext routing) {
        updateTrigger(routing, false);
    }

    @DocPath(method = PUT,
            path = "/groups/{groupId}",
            name = "Update an existing group trigger definition and its member definitions.")
    @DocParameters(value = {
            @DocParameter(name = "groupId", required = true, path = true,
                    description = "Group Trigger definition id to be updated."),
            @DocParameter(required = true, body = true, type = Trigger.class,
                    description = "Updated group trigger definition."),
            @DocParameter(name = "user", description = "User who updated the trigger"),
            @DocParameter(name = "text", description = "Additional description for the note")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Group Trigger updated.", response = Trigger.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "Trigger not found.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void updateGroupTrigger(RoutingContext routing) {
        updateTrigger(routing, true);
    }

    void updateTrigger(RoutingContext routing, boolean isGroup) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String json = routing.getBodyAsString();
                    String triggerId = routing.request().getParam("triggerId");
                    String groupId = routing.request().getParam("groupId");
                    Trigger trigger;
                    try {
                        trigger = fromJson(json, Trigger.class);
                    } catch (Exception e) {
                        log.debugf("Error parsing Trigger json: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e.toString(), e);
                    }
                    if (trigger != null && !isEmpty(triggerId)) {
                        trigger.setId(isGroup ? groupId : triggerId);
                    }
                    if (!ResponseUtil.checkTags(trigger)) {
                        throw new ResponseUtil.BadRequestException("Tags " + trigger.getTags() + " must be non empty.");
                    }
                    trigger.getLifecycle().clear();
                    trigger.addLifecycle(Trigger.TriggerLifecycle.MODIFIED, 0, parseNotes(routing));
                    try {
                        if (isGroup) {
                            definitionsService.updateGroupTrigger(tenantId, trigger);
                        } else {
                            definitionsService.updateTrigger(tenantId, trigger, true);
                        }
                        log.debugf("Trigger: %s", trigger);
                        future.complete();
                    } catch (NotFoundException e) {
                        throw new NotFoundException(e.getMessage());
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = PUT,
            path = "/groups/members/{memberId}/orphan",
            name = "Make a non-orphan member trigger into an orphan.")
    @DocParameters(value = {
            @DocParameter(name = "memberId", required = true, path = true,
                    description = "Member Trigger id to be made an orphan.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Trigger updated.", response = Trigger.class),
            @DocResponse(code = 404, message = "Trigger not found.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void orphanMemberTrigger(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String memberId = routing.request().getParam("memberId");
                    try {
                        Trigger child = definitionsService.orphanMemberTrigger(tenantId, memberId);
                        log.debugf("Orphan Member Trigger: %s", child);
                        future.complete();
                    } catch (NotFoundException e) {
                        throw new ResponseUtil.NotFoundException(e.getMessage());
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = PUT,
            path = "/{triggerId}/dampenings/{dampeningId}",
            name = "Update an existing dampening definition.",
            notes = "Note that the trigger mode can not be changed. + \n" +
                    "Return Dampening updated.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be retrieved."),
            @DocParameter(name = "dampeningId", required = true, path = true,
                    description = "Updated dampening definition."),
            @DocParameter(required = true, body = true, type = Dampening.class,
                    description = "Updated dampening definition.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Dampening Updated.", response = Dampening.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "No Dampening Found.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void updateDampening(RoutingContext routing) {
        updateDampening(routing, false);
    }

    @DocPath(method = PUT,
            path = "/groups/{groupId}/dampenings/{dampeningId}",
            name = "Update an existing group dampening definition.",
            notes = "Note that the trigger mode can not be changed. + \n" +
                    "Return Dampening updated.")
    @DocParameters(value = {
            @DocParameter(name = "groupId", required = true, path = true,
                    description = "Group trigger definition id to be retrieved."),
            @DocParameter(name = "dampeningId", required = true, path = true,
                    description = "Updated dampening definition."),
            @DocParameter(required = true, body = true, type = Dampening.class,
                    description = "Updated dampening definition.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Dampening Updated.", response = Dampening.class),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "No Dampening Found.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void updateGroupDampening(RoutingContext routing) {
        updateDampening(routing, true);
    }

    void updateDampening(RoutingContext routing, boolean isGroup) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String json = routing.getBodyAsString();
                    String triggerId = routing.request().getParam("triggerId");
                    String groupId = routing.request().getParam("groupId");
                    String dampeningId = routing.request().getParam("dampeningId");
                    Dampening dampening;
                    try {
                        dampening = fromJson(json, Dampening.class);
                    } catch (Exception e) {
                        log.debugf("Error parsing Dampening json: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e.toString());
                    }
                    Dampening found;
                    try {
                        found = definitionsService.getDampening(tenantId, dampeningId);
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                    if (found == null) {
                        throw new ResponseUtil.NotFoundException("No dampening found for dampeningId: " + dampeningId);
                    }
                    try {
                        dampening.setTriggerId(isGroup ? groupId : triggerId);
                        Dampening d = ResponseUtil.getCleanDampening(dampening);
                        log.debugf("Dampening: %s", d);
                        if (isGroup) {
                            definitionsService.updateGroupDampening(tenantId, d);
                        } else {
                            definitionsService.updateDampening(tenantId, d);
                        }
                        future.complete(d);
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = PUT,
            path = "/groups/members/{memberId}/unorphan",
            name = "Make an orphan member trigger into an group trigger.")
    @DocParameters(value = {
            @DocParameter(name = "memberId", required = true, path = true,
                    description = "Orphan Member Trigger id to be assigned into a group trigger"),
            @DocParameter(name = "memberTrigger", required = true, body = true, type = UnorphanMemberInfo.class,
                    description = "Only context and dataIdMap are used when changing back to a non-orphan.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Trigger updated.", response = Trigger.class),
            @DocResponse(code = 404, message = "Trigger not found.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void unorphanMemberTrigger(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String json = routing.getBodyAsString();
                    String memberId = routing.request().getParam("memberId");
                    UnorphanMemberInfo unorphanMemberInfo;
                    try {
                        unorphanMemberInfo = fromJson(json, UnorphanMemberInfo.class);
                    } catch (Exception e) {
                        log.debugf("Error parsing UnorphanMemberInfo json: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e.toString());
                    }
                    if (!ResponseUtil.checkTags(unorphanMemberInfo)) {
                        throw new ResponseUtil.BadRequestException("Tags " + unorphanMemberInfo.getMemberTags() + " must be non empty.");
                    }
                    try {
                        Trigger child = definitionsService.unorphanMemberTrigger(tenantId, memberId,
                                unorphanMemberInfo.getMemberContext(),
                                unorphanMemberInfo.getMemberTags(),
                                unorphanMemberInfo.getDataIdMap());
                        log.debugf("Member Trigger: %s",child);
                        future.complete();
                    } catch (NotFoundException e) {
                        throw new ResponseUtil.NotFoundException(e.getMessage());
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = PUT,
            path = "/{triggerId}/conditions",
            name = "Set the conditions for the trigger. ",
            notes = "This sets the conditions for all trigger modes, " +
                    "replacing existing conditions for all trigger modes. Returns the new conditions.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "The relevant Trigger."),
            @DocParameter(required = true, body = true, type = Condition.class, typeContainer = "List",
                    description = "Collection of Conditions to set.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Condition Set created.", response = Condition.class, responseContainer = "List"),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "Trigger not found.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void setAllConditions(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String json = routing.getBodyAsString();
                    String triggerId = routing.request().getParam("triggerId");
                    Collection<Condition> conditions;
                    try {
                        conditions = collectionFromJson(json, Condition.class);
                    } catch (Exception e) {
                        log.debugf("Error parsing Condition json: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e.getMessage());
                    }
                    try {
                        Collection<Condition> updatedConditions = definitionsService.setAllConditions(tenantId,
                                triggerId, conditions);
                        log.debugf("Conditions: %s", updatedConditions);
                        future.complete(updatedConditions);
                    } catch (NotFoundException e) {
                        throw new ResponseUtil.NotFoundException(e.getMessage());
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = PUT,
            path = "/{triggerId}/conditions/{triggerMode}",
            name = "Set the conditions for the trigger. ",
            notes = "This sets the conditions for the trigger. " +
                    "This replaces any existing conditions. Returns the new conditions.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "The relevant Trigger."),
            @DocParameter(name = "triggerMode", required = true, path = true,
                    description = "The trigger mode.",
                    allowableValues = "FIRING or AUTORESOLVE (not case sensitive)"),
            @DocParameter(required = true, body = true, type = Condition.class, typeContainer = "List",
                    description = "Collection of Conditions to set.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Condition Set created.", response = Condition.class, responseContainer = "List"),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "Trigger not found.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void setConditions(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String json = routing.getBodyAsString();
                    String triggerId = routing.request().getParam("triggerId");
                    String triggerMode = routing.request().getParam("triggerMode");
                    Collection<Condition> conditions;
                    try {
                        conditions = collectionFromJson(json, Condition.class);
                    } catch (Exception e) {
                        log.debugf("Error parsing Condition json: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e.getMessage());
                    }
                    Mode mode = Mode.valueOf(triggerMode.toUpperCase());
                    for (Condition condition : conditions) {
                        condition.setTriggerId(triggerId);
                        if (condition.getTriggerMode() == null || !condition.getTriggerMode().equals(mode)) {
                            throw new ResponseUtil.BadRequestException(
                                    "Condition: " + condition + " has a different triggerMode [" + triggerMode + "]");
                        }
                        try {
                            condition.validate();
                        } catch(IllegalArgumentException e) {
                            throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                        }
                    }
                    try {
                        Collection<Condition> updatedConditions = definitionsService.setConditions(tenantId, triggerId,
                                mode, conditions);
                        log.debugf("Conditions: %s", updatedConditions);
                        future.complete(updatedConditions);
                    } catch (NotFoundException e) {
                        throw new ResponseUtil.NotFoundException(e.getMessage());
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = PUT,
            path = "/groups/{groupId}/conditions",
            name = "Set the conditions for the group trigger. ",
            notes = "This replaces any existing conditions on the group and member conditions " +
                    "for all trigger modes. + \n" +
                    "Return the new group conditions.")
    @DocParameters(value = {
            @DocParameter(name = "groupId", required = true, path = true,
                    description = "The relevant Group definition id"),
            @DocParameter(required = true, body = true, type = GroupConditionsInfo.class, typeContainer = "List",
                    description = "Collection of Conditions to set and Map with tokens per dataId on members.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Group Condition Set created.", response = Condition.class, responseContainer = "List"),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "Trigger not found.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    @SuppressWarnings("unchecked")
    public void setGroupConditions(RoutingContext routing) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    String json = routing.getBodyAsString();
                    String groupId = routing.request().getParam("groupId");
                    String triggerMode = routing.request().getParam("triggerMode");
                    GroupConditionsInfo groupConditionsInfo;
                    try {
                        groupConditionsInfo = fromJson(json, GroupConditionsInfo.class);
                    } catch (Exception e) {
                        log.debugf("Error parsing GroupConditionsInfo json: %s. Reason: %s", json, e.toString());
                        throw new ResponseUtil.BadRequestException(e.getMessage());
                    }
                    Collection<Condition> updatedConditions = new HashSet<>();
                    if (groupConditionsInfo == null) {
                        throw new ResponseUtil.BadRequestException("GroupConditionsInfo must be non null.");
                    }
                    if (groupConditionsInfo.getConditions() == null) {
                        groupConditionsInfo.setConditions(Collections.EMPTY_LIST);
                    }
                    for (Condition condition : groupConditionsInfo.getConditions()) {
                        if (condition == null) {
                            throw new ResponseUtil.BadRequestException("GroupConditionsInfo must have non null conditions: " + groupConditionsInfo);
                        }
                        condition.setTriggerId(groupId);
                    }
                    if (triggerMode == null) {
                        Collection<Condition> firingConditions = groupConditionsInfo.getConditions().stream()
                                .filter(c -> c.getTriggerMode() == null || c.getTriggerMode().equals(Mode.FIRING))
                                .collect(Collectors.toList());
                        Collection<Condition> autoResolveConditions = groupConditionsInfo.getConditions().stream()
                                .filter(c -> c.getTriggerMode().equals(Mode.AUTORESOLVE))
                                .collect(Collectors.toList());
                        try {
                            updatedConditions.addAll(definitionsService.setGroupConditions(tenantId, groupId, Mode.FIRING, firingConditions,
                                    groupConditionsInfo.getDataIdMemberMap()));
                            updatedConditions.addAll(definitionsService.setGroupConditions(tenantId, groupId, Mode.AUTORESOLVE,
                                    autoResolveConditions,
                                    groupConditionsInfo.getDataIdMemberMap()));
                        } catch (NotFoundException e) {
                            throw new ResponseUtil.NotFoundException(e.getMessage());
                        } catch (IllegalArgumentException e) {
                            throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                        } catch (Exception e) {
                            log.debug(e.getMessage(), e);
                            throw new ResponseUtil.InternalServerException(e.toString());
                        }
                        log.debugf("Conditions: %s", updatedConditions);
                        future.complete(updatedConditions);
                        return;
                    }
                    Mode mode = Mode.valueOf(triggerMode.toUpperCase());
                    for (Condition condition : groupConditionsInfo.getConditions()) {
                        if (condition == null) {
                            throw new ResponseUtil.BadRequestException("GroupConditionsInfo must have non null conditions: " + groupConditionsInfo);
                        }
                        condition.setTriggerId(groupId);
                        if (condition.getTriggerMode() == null || !condition.getTriggerMode().equals(mode)) {
                            throw new ResponseUtil.BadRequestException("Condition: " + condition + " has a different triggerMode [" + triggerMode + "]");
                        }
                    }
                    try {
                        updatedConditions = definitionsService.setGroupConditions(tenantId, groupId, mode,
                                groupConditionsInfo.getConditions(),
                                groupConditionsInfo.getDataIdMemberMap());
                        log.debugf("Conditions: %s", updatedConditions);
                        future.complete(updatedConditions);
                    } catch (NotFoundException e) {
                        throw new ResponseUtil.NotFoundException(e.getMessage());
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    @DocPath(method = PUT,
            path = "/groups/{groupId}/conditions/{triggerMode}",
            name = "Set the conditions for the group trigger. ",
            notes = "This replaces any existing conditions on the group and member conditions. " +
                    "Return the new group conditions.")
    @DocParameters(value = {
            @DocParameter(name = "groupId", required = true, path = true,
                    description = "The relevant Group definition id."),
            @DocParameter(name = "triggerMode", required = true, path = true,
                    description = "FIRING or AUTORESOLVE (not case sensitive)"),
            @DocParameter(required = true, body = true, type = GroupConditionsInfo.class, typeContainer = "List",
                    description = "Collection of Conditions to set and Map with tokens per dataId on members.")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Group Condition Set created.", response = Condition.class, responseContainer = "List"),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "Trigger not found.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void setGroupConditionsTriggerMode(RoutingContext routing) {
        setGroupConditions(routing);
    }

    @DocPath(method = PUT,
            path = "/enabled",
            name = "Update triggers to be enabled or disabled.")
    @DocParameters(value = {
            @DocParameter(name = "triggerIds", required = true,
                    description = "List of trigger ids to enable or disable",
                    allowableValues = "Comma separated list of triggerIds to be enabled or disabled."),
            @DocParameter(name = "enabled", required = true, type = Boolean.class,
                    description = "Set enabled or disabled."),
            @DocParameter(name = "user", description = "User who enabled or disabled the trigger"),
            @DocParameter(name = "text", description = "Additional description for the note")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Triggers updated."),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "Trigger not found", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void setTriggersEnabled(RoutingContext routingContext) {
        setTriggersEnabled(routingContext, false, parseNotes(routingContext));
    }

    @DocPath(method = PUT,
            path = "/{triggerId}/enable",
            name = "Enable trigger.")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be enabled."),
            @DocParameter(name = "user", description = "User who enabled the trigger"),
            @DocParameter(name = "text", description = "Additional description for the note")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Trigger enabled."),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "Trigger not found", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void setTriggerEnabled(RoutingContext routingContext) {
        setTriggersEnabled(routingContext, false, parseNotes(routingContext));
    }

    @DocPath(method = DELETE,
            path = "/{triggerId}/enable",
            name = "Disable trigger,")
    @DocParameters(value = {
            @DocParameter(name = "triggerId", required = true, path = true,
                    description = "Trigger definition id to be disabled."),
            @DocParameter(name = "user", description = "User who disabled the trigger"),
            @DocParameter(name = "text", description = "Additional description for the note")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Trigger disabled."),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "Trigger not found", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void setTriggerDisabled(RoutingContext routingContext) {
        setTriggersEnabled(routingContext, false, parseNotes(routingContext));
    }

    @DocPath(method = PUT,
            path = "/groups/enabled",
            name = "Update group triggers and their member triggers to be enabled or disabled.")
    @DocParameters(value = {
            @DocParameter(name = "triggerIds", required = true,
                    description = "List of group trigger ids to enable or disable",
                    allowableValues = "Comma separated list of group triggerIds to be enabled or disabled."),
            @DocParameter(name = "enabled", required = true, type = Boolean.class,
                    description = "Set enabled or disabled."),
            @DocParameter(name = "user", description = "User who enabled or disabled the group trigger"),
            @DocParameter(name = "text", description = "Additional description for the note")
    })
    @DocResponses(value = {
            @DocResponse(code = 200, message = "Success, Group Triggers updated."),
            @DocResponse(code = 400, message = "Bad Request/Invalid Parameters.", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 404, message = "Group Trigger not found", response = ResponseUtil.ApiError.class),
            @DocResponse(code = 500, message = "Internal server error.", response = ResponseUtil.ApiError.class)
    })
    public void setGroupTriggersEnabled(RoutingContext routingContext) {
        setTriggersEnabled(routingContext, true, parseNotes(routingContext));
    }

    void setTriggersEnabled(RoutingContext routing, boolean isGroup, Note note) {
        routing.vertx()
                .executeBlocking(future -> {
                    String tenantId = ResponseUtil.checkTenant(routing);
                    try {
                        Boolean enabled = null;
                        String triggerIds = routing.request().getParam("triggerId");
                        if(triggerIds != null) {
                            // Single enable / disable
                            switch(routing.request().method().name()) {
                                case DELETE:
                                    enabled = false;
                                    break;
                                case PUT:
                                    enabled = true;
                                    break;
                                default:
                                    throw new ResponseUtil.BadRequestException("Invalid HTTP method");
                            }
                        } else {
                            if (routing.request().params().get(PARAM_TRIGGER_IDS) != null) {
                                triggerIds = routing.request().params().get(PARAM_TRIGGER_IDS);
                            }
                            if (routing.request().params().get(PARAM_ENABLED) != null) {
                                enabled = Boolean.valueOf(routing.request().params().get(PARAM_ENABLED));
                            }
                            if (isEmpty(triggerIds)) {
                                throw new ResponseUtil.BadRequestException("TriggerIds must be non empty.");
                            }
                            if (null == enabled) {
                                throw new ResponseUtil.BadRequestException("Enabled must be non-empty.");
                            }
                        }
                        if (isGroup) {
                            definitionsService.updateGroupTriggerEnablement(tenantId, triggerIds, enabled, note);
                        } else {
                            definitionsService.updateTriggerEnablement(tenantId, triggerIds, enabled, note);
                        }
                        future.complete();
                    } catch (NotFoundException e) {
                        throw new ResponseUtil.NotFoundException(e.getMessage());
                    } catch (IllegalArgumentException e) {
                        throw new ResponseUtil.BadRequestException("Bad arguments: " + e.getMessage());
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        throw new ResponseUtil.InternalServerException(e.toString());
                    }
                }, res -> ResponseUtil.result(routing, res));
    }

    Note parseNotes(RoutingContext routing) {
        String user = routing.request().getParam("user");
        String text = routing.request().getParam("text");
        if(isEmpty(user) && isEmpty(text)) {
            return null;
        }
        return new Note(user, text);
    }

    TriggersCriteria buildCriteria(MultiMap params) {
        String triggerIds = null;
        String query;
        boolean thin = false;

        if (params.get(PARAM_TRIGGER_IDS) != null) {
            triggerIds = params.get(PARAM_TRIGGER_IDS);
        }
        if (params.get(PARAM_THIN) != null) {
            thin = Boolean.valueOf(params.get(PARAM_THIN));
        }
        query = params.get(PARAM_QUERY);

        TriggersCriteria criteria = new TriggersCriteria();
        if (!isEmpty(triggerIds)) {
            criteria.setTriggerIds(Arrays.asList(triggerIds.split(",")));
        }
        if(!isEmpty(query)) {
            criteria.setQuery(query);
        }
        criteria.setThin(thin);

        return criteria;
    }
}
