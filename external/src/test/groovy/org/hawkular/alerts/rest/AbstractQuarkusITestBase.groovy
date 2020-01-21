package org.hawkular.alerts.rest

import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.junit.jupiter.api.BeforeAll

import java.util.concurrent.atomic.AtomicInteger

/**
 * Base class for REST tests.
 *
 * @author Lucas Ponce
 */
abstract class AbstractQuarkusITestBase {

    static baseURI = 'http://127.0.0.1:8081/hawkular/alerts/'
    static RESTClient client
    static String tenantHeaderName = "Hawkular-Tenant"
    static final String TENANT_PREFIX = UUID.randomUUID().toString()
    static final AtomicInteger TENANT_ID_COUNTER = new AtomicInteger(0)
    static String testTenant
    static Object failureEntity

    AbstractQuarkusITestBase() {
    }

    @BeforeAll
    static void initClient() {
        testTenant = nextTenantId()
        client = new RESTClient(baseURI, ContentType.JSON)
        // this prevents 404 from being wrapped in an Exception, just return the response, better for testing
        def jsonSlurper = new JsonSlurper()
        client.handler.failure = { resp ->
            failureEntity = null
            if (resp.entity != null && resp.entity.contentLength != 0) {
                def baos = new ByteArrayOutputStream()
                resp.entity.writeTo(baos)
                failureEntity = jsonSlurper.parseText(new String(baos.toByteArray(), "UTF-8"))
            }
            return resp
        }
        client.headers.put(tenantHeaderName, testTenant)
    }

    static String nextTenantId() {
        return "T${TENANT_PREFIX}${TENANT_ID_COUNTER.incrementAndGet()}"
    }

    static void waitDefinitions() {
        Thread.sleep(100)
    }
}
