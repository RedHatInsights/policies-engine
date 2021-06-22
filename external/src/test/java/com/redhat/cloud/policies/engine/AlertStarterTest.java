package com.redhat.cloud.policies.engine;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static com.redhat.cloud.policies.engine.AlertStarter.FILTER_REGEX;
import static org.junit.jupiter.api.Assertions.*;

class AlertStarterTest {

    final String logMessage = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /q/health HTTP/1.1\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
    final String logMessage2 = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /q/health HTTP/1.1\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"";
    final String logMessage3 = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /q/health/ready HTTP/1.1\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
    final String kubeMessage = "1.2.3.4 - -unset- 10/Jun/2021:07:26:00 +0000 \"GET /health/live HTTP/1.1\" 200 231 \"-\" \"kube-probe/1.20\"], tags=[level=INFO, host=notifications-gw-bla-bla";

    @Test
    void shouldMatchLogLine() {
        assertTrue(Pattern.matches(FILTER_REGEX, logMessage));
    }

    @Test
    void shouldMatchLogLine2() {
        assertTrue(Pattern.matches(FILTER_REGEX, logMessage2));
    }

    @Test
    void shouldMatchLogLine3() {
        assertTrue(Pattern.matches(FILTER_REGEX, logMessage3));
    }

    @Test
    void shouldMatchKubeMessage() {
        assertTrue(Pattern.matches(FILTER_REGEX, kubeMessage));
    }


    @Test
    void shouldMatchWhenHttpVersionIsTwo() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /q/health HTTP/2.0\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertTrue(Pattern.matches(FILTER_REGEX, inputWithHttpTwoZero));
    }

    @Test
    void shouldMatchWithMetrics() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /metrics HTTP/1.1\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertTrue(Pattern.matches(FILTER_REGEX, inputWithHttpTwoZero));
    }

    @Test
    void shouldMatchWithQMetrics() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /q/metrics HTTP/1.1\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertTrue(Pattern.matches(FILTER_REGEX, inputWithHttpTwoZero));
    }

    @Test
    void shouldNotMatchWithSomethingelse() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /my-own-api-endpoint HTTP/1.1\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertFalse(Pattern.matches(FILTER_REGEX, inputWithHttpTwoZero));
    }

    @Test
    void shouldNotMatchWhenHttpStatusIs500() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /q/health HTTP/2.0\" 500 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertFalse(Pattern.matches(FILTER_REGEX, inputWithHttpTwoZero));
    }

    @Test
    void shouldNotMatchWhenHttpStatusIs404() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /q/health HTTP/2.0\" 404 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertFalse(Pattern.matches(FILTER_REGEX, inputWithHttpTwoZero));
    }

    @Test
    void shouldMatchWhenThereIsSomethingBehindHealth() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /q/health/live HTTP/2.0\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertTrue(Pattern.matches(FILTER_REGEX, inputWithHttpTwoZero.trim()));
    }

    @Test
    void shouldMatchWithHealthReady() {
        String inputWithHttpTwoZero = "127.0.0.1 - - 09/Jun/2021:16:07:07 +0200 \"GET /q/health/ready HTTP/2.0\" 200 46 \"-\" \"Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:88.0) Gecko/20100101 Firefox/88.0\"\n";
        assertTrue(Pattern.matches(FILTER_REGEX, inputWithHttpTwoZero.trim()));
    }

    @Test
    void shouldFilterDummyHawkularTriggersWhenHttpStatusIs200() {
        String inputWithHttpTwoZero = "2021-06-22 00:47:44,003 INFO  [access_log] (vert.x-eventloop-thread-0) 10.131.7.144 - - 22/Jun/2021:00:47:44 +0000 \"GET /hawkular/alerts/triggers?triggerIds=dummy HTTP/1.1\" 200 2 \"-\" \"Apache-HttpClient/4.5.13 (Java/11.0.11)\"";
        assertTrue(Pattern.matches(FILTER_REGEX, inputWithHttpTwoZero));
    }

    @Test
    void shouldNotFilterDummyHawkularTriggersWhenHttpStatusIs400() {
        String input ="2021-06-22 08:22:32,005 INFO  [access_log] (vert.x-eventloop-thread-0) 10.131.8.85 - - 22/Jun/2021:08:22:32 +0000 \"GET /hawkular/alerts/triggers?triggerIds=dummy HTTP/1.1\" 400 2 \"-\" \"Apache-HttpClient/4.5.13 (Java/11.0.11)\"";
        assertFalse(Pattern.matches(FILTER_REGEX, input));
    }
}