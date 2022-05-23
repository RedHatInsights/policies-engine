package com.redhat.cloud.policies.engine.condition;

import com.redhat.cloud.policies.engine.process.Event;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.redhat.cloud.policies.engine.condition.ConditionParser.evaluate;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExprComplexTypeTest {

    @Test
    void testFactsAssumedParsingLogic() {
        Event event = new Event();
        event.setFacts(createFacts());

        // Simple comparison (equals)
        assertTrue(evaluate(event, "facts.arch = 'x86_64'"));

        // We can also add additional (AND or OR) statements and check that an array contains an exact match of 'avx2'
        assertTrue(evaluate(event, "facts.arch = 'x86_64' AND facts.cpu_flags contains 'avx2'"));

        // The clauses can be used to make the rules more complex or easier to read. Also, a NOT word (or !) negates the result.
        // In this case, the contains is matching a string pattern, meaning anywhere in the string.
        assertTrue(evaluate(event, "(facts.arch = 'x86_64' AND facts.cpu_flags contains 'avx2') AND NOT(facts.cloud_provider contains 'gce')"));

        // Multiple possibilities can be written with the IN operator which is similar to the one in SQL
        assertTrue(evaluate(event, "facts.cloud_provider IN ['azure_north', 'azure_west']"));

        // For exact match of all possible contains (in a string), we can use contains with array:
        assertTrue(evaluate(event, "facts.cloud_provider contains ['west', 'azure']"));

        // Numerical operators are also available, such as greater than or equal
        assertTrue(evaluate(event, "facts.number_of_cpus >= 1"));

        // And the comparisons work with mixed floating points and integers.
        assertTrue(evaluate(event, "facts.os_release > 7"));

        // Inner structures with a "name" parameter can be accessed as the map name, which is separated by dot. Also, if we leave the operator
        // we can evaluate if the identifier exists in the input.
        assertTrue(evaluate(event, "facts.network_interfaces.enp0s3 AND facts.network_interfaces.enp0s3.ipv4_addresses contains '10.0.2.15'"));
    }

    Map<String, Object> createFacts() {
        Map<String, Object> facts = new HashMap<>();
        facts.put("arch", "x86_64");
        facts.put("cloud_provider", "azure_west");

        List<String> cpuFlags = new ArrayList<>();
        cpuFlags.add("sse");
        cpuFlags.add("sse2");
        cpuFlags.add("sse4_1");
        cpuFlags.add("sse4_2");
        cpuFlags.add("avx2");
        facts.put("cpu_flags", cpuFlags);

        facts.put("os_release", "7.5");
        facts.put("last_boot_time", "2019-09-04T08:43:13"); // TODO Date object and Date object handling!
        facts.put("number_of_cpus", 1);

        List<String> enabledServices = new ArrayList<>();
        enabledServices.add("crond");
        enabledServices.add("dbus");
        enabledServices.add("fstrim");
        enabledServices.add("rhel-configure");
        facts.put("enabled_services", enabledServices);

        facts.put("bios_release_data", "12/01/2006");
        facts.put("os_kernel_version", "3.10.0");

        List<String> installedPackages = new ArrayList<>();
        installedPackages.add("0:GeoIP-1.5.0-11.el7");
        installedPackages.add("0:Red_Hat_Enterprise_Linux-Release_Notes-7-en-US-7-2.el7");
        installedPackages.add("0:openssh-7.4p1-16.el7");
        installedPackages.add("10:qemu-guest-agent-2.8.0-2.el7");
        installedPackages.add("1:NetworkManager-1.10.2-9.el7");

        Map<String, Object> networkInterfaces = new HashMap<>();
        Map<String, Object> enp0s3 = new HashMap<>();
        enp0s3.put("mtu", 1500);
        enp0s3.put("state", "UP");
        List<String> ipv4 = new ArrayList<>();
        ipv4.add("10.0.2.15");
        enp0s3.put("ipv4_addresses", ipv4);
        networkInterfaces.put("enp0s3", enp0s3);
        facts.put("network_interfaces", networkInterfaces);

        facts.put("insights_client_version", "3.0.6-2.el7_6");

        Map<String, Object> yumRepos = new HashMap<>();
        Map<String, Object> extraRepo = new HashMap<>();
        extraRepo.put("name", "Extra Packages for Enterprise Linux 7 - $basearch");
        extraRepo.put("enabled", true);
        yumRepos.put((String) extraRepo.get("name"), extraRepo);
        facts.put("yum_repos", yumRepos);

        return facts;
    }
}
