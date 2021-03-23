package com.redhat.cloud.policies.engine.actions.plugins;

import org.hawkular.alerts.actions.api.Plugin;

import javax.enterprise.context.Dependent;

// Todo: This will be deleted eventually, as the notification service will be providing the emails
@Plugin(name = "email")
@Dependent
public class EmailActionPluginListener extends NotificationActionPluginListener {

}
