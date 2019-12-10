package org.hawkular.alerts;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class AlertsServiceProducer {

    private static AlertsStandalone instance;

    static {
        instance = new AlertsStandalone();
    }

    @Produces
    public synchronized AlertsStandalone getAlertsStandalone() {
        System.out.println("Returning instance: " + instance.toString());
        return instance;
    }
}
