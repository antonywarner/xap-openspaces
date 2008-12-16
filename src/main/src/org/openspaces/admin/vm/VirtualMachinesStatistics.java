package org.openspaces.admin.vm;

/**
 * @author kimchy
 */
public interface VirtualMachinesStatistics {

    boolean isNA();

    long getTimestamp();

    long getPreviousTimestamp();

    VirtualMachinesStatistics getPrevious();

    VirtualMachinesDetails getDetails();

    int getSize();

    long getUptime();

    long getMemoryHeapCommittedInBytes();
    double getMemoryHeapCommittedInMB();
    double getMemoryHeapCommittedInGB();

    long getMemoryHeapUsedInBytes();
    double getMemoryHeapUsedInMB();
    double getMemoryHeapUsedInGB();

    /**
     * Returns the memory heap percentage from used to the max.
     */
    double getMemoryHeapPerc();

    /**
     * Returns the memory heap percentage from used to committed.
     */
    double getMemoryHeapCommittedPerc();

    long getMemoryNonHeapCommittedInBytes();
    double getMemoryNonHeapCommittedInMB();
    double getMemoryNonHeapCommittedInGB();

    long getMemoryNonHeapUsedInBytes();
    double getMemoryNonHeapUsedInMB();
    double getMemoryNonHeapUsedInGB();

    /**
     * Returns the memory non heap percentage from used to the max.
     */
    double getMemoryNonHeapPerc();

    /**
     * Returns the memory non heap percentage from used to committed.
     */
    double getMemoryNonHeapCommittedPerc();

    int getThreadCount();

    int getPeakThreadCount();

    long getGcCollectionCount();

    long getGcCollectionTime();

    /**
     * The percentage of the gc collection time between the current sampled statistics
     * and the previous one.
     */
    double getGcCollectionPerc();
}