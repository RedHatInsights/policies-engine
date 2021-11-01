package com.redhat.cloud.policies.engine.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.scheduler.Scheduled;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;


/**
 * Exports the following from /proc/self/status. See proc(5)
 * VmHWM:    265580 kB
 * VmRSS:    233156 kB
 * RssAnon:          210444 kB
 * RssFile:           22712 kB
 * VmStk:       136 kB
 * VmLib:     24416 kB
 * VmData:  3529900 kB
 * VmSize:  13529900 kB
 * Threads: 23
 */
@ApplicationScoped
public class ProcSelfStatusExporter {

    private final Logger log = Logger.getLogger(this.getClass().getSimpleName());

    private static final String PATHNAME = "/proc/self/status";

    private boolean hasWarned = false;

    private long vmHwm;
    Gauge vmHwmGauge;

    private long vmRss;
    Gauge vmRssGauge;

    private long rssAnon;
    Gauge rssAnonGauge;

    private long rssFile;
    Gauge rssFileGauge;

    private long vmStk;
    Gauge vmStkGauge;

    private long vmLib;
    Gauge vmLibGauge;

    private long vmData;
    Gauge vmDataGauge;

    private long vmSize;
    Gauge vmSizeGauge;

    private int threads;
    Gauge threadsGauge;

    public ProcSelfStatusExporter(MeterRegistry registry) {
        vmHwmGauge = Gauge.builder("status.vmHwm", () -> new AtomicLong(vmHwm)).tags("type=proc").register(registry);
        vmRssGauge = Gauge.builder("status.vmRss", () -> new AtomicLong(vmRss)).tags("type=proc").register(registry);
        rssAnonGauge = Gauge.builder("status.rssAnon", () -> new AtomicLong(rssAnon)).tags("type=proc").register(registry);
        rssFileGauge = Gauge.builder("status.rssFile", () -> new AtomicLong(rssFile)).tags("type=proc").register(registry);
        vmStkGauge = Gauge.builder("status.vmStk", () -> new AtomicLong(vmStk)).tags("type=proc").register(registry);
        vmLibGauge = Gauge.builder("status.vmLib", () -> new AtomicLong(vmLib)).tags("type=proc").register(registry);
        vmDataGauge = Gauge.builder("status.vmData", () -> new AtomicLong(vmData)).tags("type=proc").register(registry);
        vmSizeGauge = Gauge.builder("status.vmSize", () -> new AtomicLong(vmSize)).tags("type=proc").register(registry);
        threadsGauge = Gauge.builder("status.threads", () -> new AtomicLong(threads)).tags("type=proc").register(registry);
    }

    @Scheduled(every = "10s")
    void gather() {
        File status = new File(PATHNAME);
        if (!status.exists() || !status.canRead()) {
            if (!hasWarned) {
                log.warning("Can't read " + PATHNAME);
                hasWarned = true;
            }
            return;
        }

        try (Scanner fr = new Scanner(status)) {
            while (fr.hasNextLine()) {
                String line = fr.nextLine();
                String[] parts = line.split("[ \t]+");

                switch (parts[0]) {
                    case "VmHWM:":
                        vmHwm = Long.parseLong(parts[1]);
                        break;
                    case "VmRSS:":
                        vmRss = Long.parseLong(parts[1]);
                        break;
                    case "RssAnon:":
                        rssAnon = Long.parseLong(parts[1]);
                        break;
                    case "RssFile:":
                        rssFile = Long.parseLong(parts[1]);
                        break;
                    case "VmStk:":
                        vmStk = Long.parseLong(parts[1]);
                        break;
                    case "VmLib:":
                        vmLib = Long.parseLong(parts[1]);
                        break;
                    case "VmData:":
                        vmData = Long.parseLong(parts[1]);
                        break;
                    case "VmSize:":
                        vmSize = Long.parseLong(parts[1]);
                        break;
                    case "Threads:":
                        threads = Integer.parseInt(parts[1]);
                        break;
                    default:
                        // Nothing. File has more entries that we are not interested in.
                }
            }
        } catch (Exception e) {
            log.warning("Scanning of file failed: " + e.getMessage());
        }
    }

    public long getVmHwm() {
        return vmHwm;
    }

    public long getVmRss() {
        return vmRss;
    }

    public long getRssAnon() {
        return rssAnon;
    }

    public long getRssFile() {
        return rssFile;
    }

    public long getVmStk() {
        return vmStk;
    }

    public long getVmLib() {
        return vmLib;
    }

    public long getVmData() {
        return vmData;
    }

    public long getVmSize() {
        return vmSize;
    }

    public long getThreads() {
        return threads;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProcSelfStatusExporter{");
        sb.append("vmHwm=").append(vmHwm / 1024);
        sb.append(", vmRss=").append(vmRss / 1024);
        sb.append(", rssAnon=").append(rssAnon / 1024);
        sb.append(", rssFile=").append(rssFile / 1024);
        sb.append(", vmStk=").append(vmStk / 1024);
        sb.append(", vmLib=").append(vmLib / 1024);
        sb.append(", vmData=").append(vmData / 1024);
        sb.append(", vmSize=").append(vmSize / 1024);
        sb.append(", threads=").append(threads);
        sb.append('}');
        return sb.toString();
    }
}
