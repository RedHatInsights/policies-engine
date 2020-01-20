package org.hawkular.alerts.rest

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

/**
 * Alerts REST tests.
 *
 * @author Lucas Ponce
 */
@QuarkusTest
class AlertsITest extends AbstractQuarkusITestBase {

    @Test
    void findAlerts() {
        def resp = client.get(path: "")
        assert resp.status == 200 : resp.status
    }

    @Test
    void findAlertsByCriteria() {
        String now = String.valueOf(System.currentTimeMillis());
        def resp = client.get(path: "", query: [endTime:now, startTime:"0",triggerIds:"Trigger-01,Trigger-02"] )
        assert resp.status == 200 : resp.status

        resp = client.get(path: "", query: [endTime:now, startTime:"0",alertIds:"Trigger-01|"+now+","+"Trigger-02|"+now] )
        assert resp.status == 200 : resp.status

        resp = client.get(path: "", query: [endTime:now, startTime:"0",statuses:"OPEN,ACKNOWLEDGED,RESOLVED"] )
        assert resp.status == 200 : resp.status

        resp = client.get(path: "", query: [tags:"data-01|*,data-02|*"] )
        assert resp.status == 200 : resp.status

        resp = client.get(path: "", query: [tags:"dataId|data-01,dataId|data-02",thin:true] )
        assert resp.status == 200 : resp.status

        resp = client.get(path: "", query: [tagQuery:"tagA or (tagB and tagC in ['e.*', 'f.*'])"] )
        assert resp.status == 200 : resp.status

        resp = client.get(path: "", query: [endResolvedTime:now, startResolvedTime:"0"] )
        assert resp.status == 200 : resp.status

        resp = client.get(path: "", query: [endAckTime:now, startAckTime:"0"] )
        assert resp.status == 200 : resp.status

        resp = client.get(path: "", query: [endStatusTime:now, startStatusTime:"0"] )
        assert resp.status == 200 : resp.status
    }

    @Test
    void findAlertsUnknownParams() {
        String now = String.valueOf(System.currentTimeMillis());
        def resp = client.get(path: "", query: [
            startTime:"0", endTime:now,
            startAckTime:"0", endAckTime:now,
            startResolvedTime:"0", endResolvedTime:now,
            alertIds:"Alert-01", triggerIds:"Trigger-01,Trigger-02", statuses: "OPEN", severities: "LOW",
            tags: "a|b", tagQuery: "foo", thin: true] )
        assert resp.status == 200 : resp.status

        resp = client.get(path: "", query: [
            startyTime:"0", endTime:now,
            alertIds:"Alert-01", triggrIds:"Trigger-01,Trigger-02", statuses: "OPEN"])
        assert resp.status == 400 : resp.status
        assert failureEntity.errorMsg.contains("startyTime")
        assert failureEntity.errorMsg.contains("triggrIds")
        assert !failureEntity.errorMsg.contains("endTime")
        assert !failureEntity.errorMsg.contains("alertIds")
        assert !failureEntity.errorMsg.contains("statuses")
    }

    @Test
    void deleteAlerts() {
        String now = String.valueOf(System.currentTimeMillis());

        def resp = client.delete(path: "badAlertId" )
        assert resp.status == 404 : resp.status

        resp = client.put(path: "delete", query: [endTime:now, startTime:"0",triggerIds:"Trigger-01,Trigger-02"] )
        assert resp.status == 200 : resp.status

        resp = client.put(path: "delete", query: [endTime:now, startTime:"0",alertIds:"Trigger-01|"+now+","+"Trigger-02|"+now] )
        assert resp.status == 200 : resp.status

        resp = client.put(path: "delete", query: [endTime:now, startTime:"0",statuses:"OPEN,ACKNOWLEDGED,RESOLVED"] )
        assert resp.status == 200 : resp.status

        resp = client.put(path: "delete", query: [tags:"data-01|*,data-02|*"] )
        assert resp.status == 200 : resp.status

        resp = client.put(path: "delete", query: [tags:"dataId|data-01,dataId|data-02"] )
        assert resp.status == 200 : resp.status

        resp = client.put(path: "delete", query: [tagQuery:"tagA or (tagB and tagC in ['e.*', 'f.*'])"] )
        assert resp.status == 200 : resp.status

        resp = client.put(path: "delete", query: [endResolvedTime:now, startResolvedTime:"0"] )
        assert resp.status == 200 : resp.status

        resp = client.put(path: "delete", query: [endAckTime:now, startAckTime:"0"] )
        assert resp.status == 200 : resp.status

        resp = client.put(path: "delete", query: [endStatusTime:now, startStatusTime:"0"] )
        assert resp.status == 200 : resp.status
    }

}
