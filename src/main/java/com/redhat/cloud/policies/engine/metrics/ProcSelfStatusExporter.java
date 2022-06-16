package com.redhat.cloud.policies.engine.metrics;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.util.Scanner;


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
 *
 * @author hrupp
 */
@ApplicationScoped
public class ProcSelfStatusExporter {

    private static final String PATHNAME = "/proc/self/status";

    private boolean hasWarned = false;

    private long vmHwm;
    private long vmRss;
    private long rssAnon;
    private long rssFile;
    private long vmStk;
    private long vmLib;
    private long vmData;
    private long vmSize;
    private int threads;

    @Scheduled(every = "10s")
    void gather() {
        File status = new File(PATHNAME);
        if (!status.exists() || !status.canRead()) {
            if (!hasWarned) {
                Log.warn("Can't read " + PATHNAME);
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
            Log.warn("Scanning of file failed: " + e.getMessage());
        }
    }

    @Gauge(name = "status.vmHwm", absolute = true, unit = MetricUnits.KILOBYTES, tags = "type=proc")
    public long getVmHwm() {
        return vmHwm;
    }

    @Gauge(name = "status.vmRss", absolute = true, unit = MetricUnits.KILOBYTES, tags = "type=proc")
    public long getVmRss() {
        return vmRss;
    }

    @Gauge(name = "status.rssAnon", absolute = true, unit = MetricUnits.KILOBYTES, tags = "type=proc")
    public long getRssAnon() {
        return rssAnon;
    }

    @Gauge(name = "status.rssFile", absolute = true, unit = MetricUnits.KILOBYTES, tags = "type=proc")
    public long getRssFile() {
        return rssFile;
    }

    @Gauge(name = "status.vmStk", absolute = true, unit = MetricUnits.KILOBYTES, tags = "type=proc")
    public long getVmStk() {
        return vmStk;
    }

    @Gauge(name = "status.vmLib", absolute = true, unit = MetricUnits.KILOBYTES, tags = "type=proc")
    public long getVmLib() {
        return vmLib;
    }

    @Gauge(name = "status.vmData", absolute = true, unit = MetricUnits.KILOBYTES, tags = "type=proc")
    public long getVmData() {
        return vmData;
    }

    @Gauge(name = "status.vmSize", absolute = true, unit = MetricUnits.KILOBYTES, tags = "type=proc")
    public long getVmSize() {
        return vmSize;
    }

    @Gauge(name = "status.threads", absolute = true, unit = MetricUnits.NONE, tags = "type=proc")
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
