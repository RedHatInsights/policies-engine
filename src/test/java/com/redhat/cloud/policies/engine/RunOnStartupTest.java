package com.redhat.cloud.policies.engine;

import org.junit.jupiter.api.Test;

import static com.redhat.cloud.policies.engine.RunOnStartup.ACCESS_LOG_FILTER_PATTERN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunOnStartupTest {

    final String logMessage = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /health HTTP/1.1\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
    final String logMessage2 = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /health HTTP/1.1\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"";
    final String logMessage3 = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /health/ready HTTP/1.1\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
    final String kubeMessage = "1.2.3.4 - -unset- 10/Jun/2021:07:26:00 +0000 \"GET /health/live HTTP/1.1\" 200 231 \"-\" \"kube-probe/1.20\"], tags=[level=INFO, host=notifications-gw-bla-bla";

    @Test
    void shouldMatchLogLine() {
        assertTrue(ACCESS_LOG_FILTER_PATTERN.matcher(logMessage).matches());
    }

    @Test
    void shouldMatchLogLine2() {
        assertTrue(ACCESS_LOG_FILTER_PATTERN.matcher(logMessage2).matches());
    }

    @Test
    void shouldMatchLogLine3() {
        assertTrue(ACCESS_LOG_FILTER_PATTERN.matcher(logMessage3).matches());
    }

    @Test
    void shouldMatchKubeMessage() {
        assertTrue(ACCESS_LOG_FILTER_PATTERN.matcher(kubeMessage).matches());
    }


    @Test
    void shouldMatchWhenHttpVersionIsTwo() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /health HTTP/2.0\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertTrue(ACCESS_LOG_FILTER_PATTERN.matcher(inputWithHttpTwoZero).matches());
    }

    @Test
    void shouldMatchWithMetrics() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /metrics HTTP/1.1\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertTrue(ACCESS_LOG_FILTER_PATTERN.matcher(inputWithHttpTwoZero).matches());
    }

    @Test
    void shouldNotMatchWithSomethingelse() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /my-own-api-endpoint HTTP/1.1\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertFalse(ACCESS_LOG_FILTER_PATTERN.matcher(inputWithHttpTwoZero).matches());
    }

    @Test
    void shouldNotMatchWhenHttpStatusIs500() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /health HTTP/2.0\" 500 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertFalse(ACCESS_LOG_FILTER_PATTERN.matcher(inputWithHttpTwoZero).matches());
    }

    @Test
    void shouldNotMatchWhenHttpStatusIs404() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /health HTTP/2.0\" 404 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertFalse(ACCESS_LOG_FILTER_PATTERN.matcher(inputWithHttpTwoZero).matches());
    }

    @Test
    void shouldMatchWhenThereIsSomethingBehindHealth() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /health/live HTTP/2.0\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertTrue(ACCESS_LOG_FILTER_PATTERN.matcher(inputWithHttpTwoZero).matches());
    }

    @Test
    void shouldMatchWithHealthReady() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /health/ready HTTP/2.0\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertTrue(ACCESS_LOG_FILTER_PATTERN.matcher(inputWithHttpTwoZero).matches());
    }

    @Test
    void shouldFilterDummyHawkularTriggersWhenHttpStatusIs200() {
        String inputWithHttpTwoZero = "2021-06-22 00:47:44,003 INFO  [access_log] (vert.x-eventloop-thread-0) 10.131.7.144 - - 22/Jun/2021:00:47:44 +0000 \"GET /hawkular/alerts/triggers?triggerIds=dummy HTTP/1.1\" 200 2 \"-\" \"Apache-HttpClient/4.5.13 (Java/11.0.11)\"";
        assertTrue(ACCESS_LOG_FILTER_PATTERN.matcher(inputWithHttpTwoZero).matches());
    }

    @Test
    void shouldNotFilterDummyHawkularTriggersWhenHttpStatusIs400() {
        String input ="2021-06-22 08:22:32,005 INFO  [access_log] (vert.x-eventloop-thread-0) 10.131.8.85 - - 22/Jun/2021:08:22:32 +0000 \"GET /hawkular/alerts/triggers?triggerIds=dummy HTTP/1.1\" 400 2 \"-\" \"Apache-HttpClient/4.5.13 (Java/11.0.11)\"";
        assertFalse(ACCESS_LOG_FILTER_PATTERN.matcher(input).matches());
    }

    @Test
    void shouldNotMatchHawkularAlertsDifferentThanDummy() {
        String input = "10.129.19.92 - - 28/Jun/2021:16:08:37 +0000 \\\"GET /hawkular/alerts/triggers?triggerIds=3df53241-3e09-481b-a322-4892caaaaadc%2C062ba34a-6311-4468-b774-083ab770531c HTTP/1.1\\\" 200 536484 \\\"-\\\" \\\"Apache-HttpClient/4.5.13 (Java/11.0.11)\\\"";
        assertFalse(ACCESS_LOG_FILTER_PATTERN.matcher(input).matches());
    }
}
