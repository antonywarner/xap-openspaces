package org.openspaces.admin.alert.alerts;


import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.config.ReplicationChannelDisconnectedAlertConfigurer;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.SpaceInstance;

/**
 * A replication channel disconnection alert, fired upon a disconnected channel between a source
 * (primary Space) and it's target (backup Space or Mirror). The alert is raised when the channel
 * has disconnected. The alert is resolved when the channel is reconnected.
 * <p>
 * These thresholds can be configured by using the
 * {@link ReplicationChannelDisconnectedAlertConfigurer}.
 * <p>
 * This alert will be received on the call to {@link AlertTriggeredEventListener#alertTriggered(Alert)} for
 * registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationChannelDisconnectedAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;
    
    public static final String SOURCE_HOST_ADDRESS = "source-host-address";
    public static final String SOURCE_HOST_NAME = "source-host-name";
    public static final String SOURCE_CPU_UTILIZATION = "source-cpu-utilization";
    public static final String SOURCE_HEAP_UTILIZATION = "source-heap-utilization";
    
    public static final String TARGET_HOST_ADDRESS = "target-host-address";
    public static final String TARGET_HOST_NAME = "target-host-name";
    public static final String TARGET_CPU_UTILIZATION = "target-cpu-utilization";
    public static final String TARGET_HEAP_UTILIZATION = "target-heap-utilization";
    
    public static final String REPLICATION_STATUS = "replication-status";
    public static final String SOURCE_UID = "source-uid";
    public static final String TARGET_UID = "target-uid";

    /** required by java.io.Externalizable */
    public ReplicationChannelDisconnectedAlert() {
    }
    
    public ReplicationChannelDisconnectedAlert(Alert alert) {
        super(alert);
    }
    
    /**
     * The host address of the source machine that this alert corresponds to.
     * @return the host address; may be <code>null</code>.
     */
    public String getSourceHostAddress() {
        return getProperties().get(SOURCE_HOST_ADDRESS);
    }
    
    /**
     * The host name of the source machine that this alert corresponds to.
     * @return the host name; may be <code>null</code>.
     */
    public String getSourceHostName() {
        return getProperties().get(SOURCE_HOST_NAME);
    }
    
    /**
     * The CPU utilization reading when this alert was fired.
     * @return the CPU utilization; may be <code>null</code>.
     */
    public Double getSourceCpuUtilization() {
        String value = getProperties().get(SOURCE_CPU_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
    
    /**
     * The heap memory utilization reading when this alert was fired.
     * @return the heap utilization; may be <code>null</code>.
     */
    public Double getSourceHeapUtilization() {
        String value = getProperties().get(SOURCE_HEAP_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
    
    /**
     * The host address of the target machine that this alert corresponds to.
     * @return the host address; may be <code>null</code>.
     */
    public String getTargetHostAddress() {
        return getProperties().get(TARGET_HOST_ADDRESS);
    }
    
    /**
     * The host name of the target machine that this alert corresponds to.
     * @return the host name; may be <code>null</code>.
     */
    public String getTargetHostName() {
        return getProperties().get(TARGET_HOST_NAME);
    }
    
    /**
     * The CPU utilization reading when this alert was fired.
     * @return the CPU utilization; may be <code>null</code>.
     */
    public Double getTargetCpuUtilization() {
        String value = getProperties().get(TARGET_CPU_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
    
    /**
     * The heap memory utilization reading when this alert was fired.
     * @return the heap utilization; may be <code>null</code>.
     */
    public Double getTargetHeapUtilization() {
        String value = getProperties().get(TARGET_HEAP_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
    
    /**
     * The {@link ReplicationStatus replication status} from source to target Space.
     * @return the replication status; may be <code>null</code>.
     */
    public String getReplicationStatus() {
        return getProperties().get(REPLICATION_STATUS);
    }
    
    /**
     * The {@link SpaceInstance#getUid() source Space unique id}.
     * @return the source uid; may be <code>null</code>.
     */
    public String getSourceUid() {
        return getProperties().get(SOURCE_UID);
    }
    
    /**
     * The {@link SpaceInstance#getUid() target Space unique id}.
     * @return the target uid; may be <code>null</code>.
     */
    public String getTargetUid() {
        return getProperties().get(TARGET_UID);
    }
}
