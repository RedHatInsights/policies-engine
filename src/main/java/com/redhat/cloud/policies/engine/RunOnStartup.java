package com.redhat.cloud.policies.engine;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;

@Startup
public class RunOnStartup {

    // TODO POL-649 Update that pattern after the new health check call has been confirmed.
    public static final Pattern ACCESS_LOG_FILTER_PATTERN = Pattern.compile(".*(/health(/\\w+)?|/metrics|/hawkular/alerts/triggers\\?triggerIds=dummy) HTTP/[0-9].[0-9]\" 200.*\\n?");

    @ConfigProperty(name = "quarkus.http.access-log.category")
    String accessLogCategory;

    @PostConstruct
    void postConstruct() {
        initAccessLogFilter();
        logGitProperties();
    }

    private void initAccessLogFilter() {
        java.util.logging.Logger.getLogger(accessLogCategory).setFilter(logRecord ->
                !ACCESS_LOG_FILTER_PATTERN.matcher(logRecord.getMessage()).matches()
        );
    }

    private void logGitProperties() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("git.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("git.properties is not available");
            } else {
                StringBuilder result = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!line.startsWith("#Generated")) {
                            result.append(line);
                        }
                    }
                }
                Log.info(result.toString());
            }
        } catch (Exception e) {
            Log.error("Could not read git.properties", e);
        }
    }
}
