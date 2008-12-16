package org.openspaces.admin.internal.os;

import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.os.OperatingSystemsDetails;
import org.openspaces.admin.os.OperatingSystemsStatistics;
import org.openspaces.admin.support.StatisticsUtils;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemsStatistics implements OperatingSystemsStatistics {

    private final long timestamp;

    private final OperatingSystemStatistics[] stats;

    private final OperatingSystemsStatistics previousStats;

    private final OperatingSystemsDetails details;

    public DefaultOperatingSystemsStatistics(OperatingSystemStatistics[] stats, OperatingSystemsStatistics previousStats, OperatingSystemsDetails details) {
        this.timestamp = System.currentTimeMillis();
        this.stats = stats;
        this.previousStats = previousStats;
        this.details = details;
    }

    public boolean isNA() {
        return stats == null || stats.length == 0 || stats[0].isNA();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int getSize() {
        return stats.length;
    }

    public OperatingSystemsStatistics getPrevious() {
        return this.previousStats;
    }

    public OperatingSystemsDetails getDetails() {
        return this.details;
    }

    public long getCommittedVirtualMemorySizeInBytes() {
        long total = 0;
        for (OperatingSystemStatistics stat : stats) {
            if (stat.getCommittedVirtualMemorySizeInBytes() != -1) {
                total += stat.getCommittedVirtualMemorySizeInBytes();
            }
        }
        return total;
    }

    public double getCommittedVirtualMemorySizeInMB() {
        return StatisticsUtils.convertToMB(getCommittedVirtualMemorySizeInBytes());
    }

    public double getCommittedVirtualMemorySizeInGB() {
        return StatisticsUtils.convertToGB(getCommittedVirtualMemorySizeInBytes());
    }

    public long getFreeSwapSpaceSizeInBytes() {
        long total = 0;
        for (OperatingSystemStatistics stat : stats) {
            if (stat.getFreeSwapSpaceSizeInBytes() != -1) {
                total += stat.getFreeSwapSpaceSizeInBytes();
            }
        }
        return total;
    }

    public double getFreeSwapSpaceSizeInMB() {
        return StatisticsUtils.convertToMB(getFreeSwapSpaceSizeInBytes());
    }

    public double getFreeSwapSpaceSizeInGB() {
        return StatisticsUtils.convertToGB(getFreeSwapSpaceSizeInBytes());
    }

    public long getFreePhysicalMemorySizeInBytes() {
        long total = 0;
        for (OperatingSystemStatistics stat : stats) {
            if (stat.getFreePhysicalMemorySizeInBytes() != -1) {
                total += stat.getFreePhysicalMemorySizeInBytes();
            }
        }
        return total;
    }

    public double getFreePhysicalMemorySizeInMB() {
        return StatisticsUtils.convertToMB(getFreePhysicalMemorySizeInBytes());
    }

    public double getFreePhysicalMemorySizeInGB() {
        return StatisticsUtils.convertToGB(getFreePhysicalMemorySizeInBytes());
    }

    public double getTotalSystemLoadAverage() {
        double total = 0;
        for (OperatingSystemStatistics stat : stats) {
            if (stat.getSystemLoadAverage() != -1) {
                total += stat.getSystemLoadAverage();
            }
        }
        return total;
    }

    public double getSystemLoadAverage() {
        int count = 0;
        double total = 0;
        for (OperatingSystemStatistics stat : stats) {
            if (stat.getSystemLoadAverage() != -1) {
                count++;
                total += stat.getSystemLoadAverage();
            }
        }
        if (count == 0) {
            return -1;
        }
        return total / count;
    }
}
