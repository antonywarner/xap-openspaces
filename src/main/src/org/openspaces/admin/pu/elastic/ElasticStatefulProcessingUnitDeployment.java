package org.openspaces.admin.pu.elastic;

import java.io.File;
import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.AbstractElasticProcessingUnitDeployment;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualMemoryCapacityScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.ManualMemoryCapacityScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.isolation.DedicatedIsolation;
import org.openspaces.admin.pu.elastic.isolation.PublicIsolation;
import org.openspaces.admin.pu.elastic.isolation.SharedTenantIsolation;
import org.openspaces.admin.pu.elastic.topology.ElasticStatefulDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

import com.gigaspaces.security.directory.UserDetails;

/**
 * Defines an elastic deployment of a processing unit that with an embedded space..
 * 
 * The advantage of this topology is that the code can access the data without
 * the network/serialization overhead and that space events can be used as code triggers.
 * The disadvantage compared to a stateless processing unit is that the ratio between 
 * the minimum and maximum number of containers is limited.
 * 
 * @author itaif
 */
public class ElasticStatefulProcessingUnitDeployment extends AbstractElasticProcessingUnitDeployment implements ElasticStatefulDeploymentTopology {

    public static final String MAX_MEMORY_CAPACITY_MEGABYTES_DYNAMIC_PROPERTY = "max-memory-capacity-megabytes";
    public static final String MIN_MEMORY_CAPACITY_MEGABYTES_DYNAMIC_PROPERTY = "min-memory-capacity-megabytes";
    
    Map<String,String> scaleStrategy;
    private long maxMemoryCapacityMegabytes;
    private long minMemoryCapacityMegabytes;
    private int numberOfBackups = 1;
    private int numberOfInstances;
    private int maxInstancesPerMachine = 1;
    
    /**
     * Constructs a stateful processing unit deployment based on the specified processing unit name (should
     * exists under the <code>[GS ROOT]/deploy</code> directory.
     */
    public ElasticStatefulProcessingUnitDeployment(String processingUnit) {
        super(processingUnit);
    }
    
    /**
     * Constructs a stateful processing unit deployment based on the specified processing unit file path 
     * (points either to a processing unit jar/zip file or a directory).
     */
    public ElasticStatefulProcessingUnitDeployment(File processingUnit) {
        super(processingUnit.getAbsolutePath());
    }

    public ElasticStatefulProcessingUnitDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        this.maxMemoryCapacityMegabytes = unit.toMegaBytes(maxMemoryCapacity);
        return this;
    }

    public ElasticStatefulProcessingUnitDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        this.maxMemoryCapacityMegabytes = MemoryUnit.toMegaBytes(maxMemoryCapacity);
        return this;
    }
    
    public ElasticStatefulProcessingUnitDeployment minMemoryCapacity(int minMemoryCapacity, MemoryUnit unit) {
        this.minMemoryCapacityMegabytes = unit.toMegaBytes(minMemoryCapacity);
        return this;
    }

    public ElasticStatefulProcessingUnitDeployment minMemoryCapacity(String minMemoryCapacity) {
        this.minMemoryCapacityMegabytes = MemoryUnit.toMegaBytes(minMemoryCapacity);
        return this;
    }
    
    public ElasticStatefulProcessingUnitDeployment highlyAvailable(boolean highlyAvailable) {
        numberOfBackups(highlyAvailable ? 1 : 0);
        return this;
    }
    
    /**
     * Overrides the number of backup instances per partition.
     */
    protected ElasticStatefulProcessingUnitDeployment numberOfBackups(int numberOfBackups) {
        this.numberOfBackups = numberOfBackups;
        return this;
    }
    
    /**
     * Overrides the number of partition.
     */
    protected ElasticStatefulProcessingUnitDeployment numberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
        return this;
    }

    /**
     * Overrides the number of instances from the same partition per machine.
     */
    protected ElasticStatefulProcessingUnitDeployment maxInstancesPerMachine(int maxInstancesPerMachine) {
        this.maxInstancesPerMachine  = maxInstancesPerMachine;
        return this;
    }
    
    public ElasticStatefulProcessingUnitDeployment enableScaleStrategy(EagerScaleBeanConfigurer strategy) {
        return enableScaleStrategy(strategy.getConfig());
    }

    public ElasticStatefulProcessingUnitDeployment enableScaleStrategy(ManualContainersScaleBeanConfigurer strategy) {
        return enableScaleStrategy(strategy.getConfig());
    }

    public ElasticStatefulProcessingUnitDeployment enableScaleStrategy(ManualMemoryCapacityScaleBeanConfigurer strategy) {
        return enableScaleStrategy(strategy.getConfig());
    }

    public ElasticStatefulProcessingUnitDeployment enableScaleStrategy(MemoryCapacityScaleBeanConfigurer strategy) {
        return enableScaleStrategy(strategy.getConfig());
    }
    
    public ElasticStatefulProcessingUnitDeployment enableScaleStrategy(EagerScaleBeanConfig strategy) {
        return (ElasticStatefulProcessingUnitDeployment) super.enableScaleStrategy(strategy.getBeanClassName(),strategy.getProperties());
    }

    public ElasticStatefulProcessingUnitDeployment enableScaleStrategy(ManualContainersScaleBeanConfig strategy) {
        return (ElasticStatefulProcessingUnitDeployment) super.enableScaleStrategy(strategy.getBeanClassName(), strategy.getProperties());
    }

    public ElasticStatefulProcessingUnitDeployment enableScaleStrategy(ManualMemoryCapacityScaleBeanConfig strategy) {
        return (ElasticStatefulProcessingUnitDeployment) super.enableScaleStrategy(strategy.getBeanClassName(), strategy.getProperties());
    }
    
    public ElasticStatefulProcessingUnitDeployment enableScaleStrategy(MemoryCapacityScaleConfig strategy) {
        return (ElasticStatefulProcessingUnitDeployment) super.enableScaleStrategy(strategy.getBeanClassName(), strategy.getProperties());
    }
    
    public ElasticStatefulProcessingUnitDeployment name(String name) {
        return (ElasticStatefulProcessingUnitDeployment) super.name(name);
    }

    public ElasticStatefulProcessingUnitDeployment zone(String zone) {
        return (ElasticStatefulProcessingUnitDeployment) super.zone(zone);
    }

    public ElasticStatefulProcessingUnitDeployment setContextProperty(String key, String value) {
        return (ElasticStatefulProcessingUnitDeployment) super.setContextProperty(key, value);
    }

    public ElasticStatefulProcessingUnitDeployment secured(boolean secured) {
        return (ElasticStatefulProcessingUnitDeployment) super.secured(secured);
    }

    public ElasticStatefulProcessingUnitDeployment userDetails(UserDetails userDetails) {
        return (ElasticStatefulProcessingUnitDeployment) super.userDetails(userDetails);
    }

    public ElasticStatefulProcessingUnitDeployment userDetails(String userName, String password) {
        return (ElasticStatefulProcessingUnitDeployment) super.userDetails(userName, password);
    }

    public ElasticStatefulProcessingUnitDeployment isolation(DedicatedIsolation isolation) {
        return (ElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }

    
    public ElasticStatefulProcessingUnitDeployment isolation(SharedTenantIsolation isolation) {
        return (ElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }
    
    public ElasticStatefulProcessingUnitDeployment isolation(PublicIsolation isolation) {
        return (ElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }
    
    public ElasticStatefulProcessingUnitDeployment useScript() {
        return (ElasticStatefulProcessingUnitDeployment) super.useScript();
    }

    public ElasticStatefulProcessingUnitDeployment overrideVmInputArguments() {
        return (ElasticStatefulProcessingUnitDeployment) super.overrideVmInputArguments();
    }

    public ElasticStatefulProcessingUnitDeployment vmInputArgument(String vmInputArgument) {
        return (ElasticStatefulProcessingUnitDeployment) super.vmInputArgument(vmInputArgument);
    }

    public ElasticStatefulProcessingUnitDeployment environmentVariable(String name, String value) {
        return (ElasticStatefulProcessingUnitDeployment) super.environmentVariable(name, value);
    }
    
    public ElasticStatefulProcessingUnitDeployment machinePool(String beanClassName, Map<String,String> beanProperties) {
        return (ElasticStatefulProcessingUnitDeployment) super.machinePool(beanClassName, beanProperties);
    }
    
    public ProcessingUnitDeployment toProcessingUnitDeployment() {

        ProcessingUnitDeployment deployment = super.toProcessingUnitDeployment();
        
        if (this.maxMemoryCapacityMegabytes == 0) {
            throw new IllegalStateException("maxMemoryCapacity is too low.");
        }

        if (this.maxMemoryCapacityMegabytes >0 && this.minMemoryCapacityMegabytes == 0) {
            this.minMemoryCapacityMegabytes = this.maxMemoryCapacityMegabytes;
        }

        //TODO: this is a hack for existing server side ESM implementation
        //server side should not care about max/min capacity settings.
        deployment
        .setDynamicProperty(MAX_MEMORY_CAPACITY_MEGABYTES_DYNAMIC_PROPERTY, ""+this.maxMemoryCapacityMegabytes)
        .setDynamicProperty(MIN_MEMORY_CAPACITY_MEGABYTES_DYNAMIC_PROPERTY, ""+this.minMemoryCapacityMegabytes);
        
        
        int numberOfInstances = this.numberOfInstances;
        if (numberOfInstances == 0) {
            numberOfInstances = calcNumberOfPartitionsFromMemoryRequirements();
        }
        
        deployment
        .maxInstancesPerMachine(this.maxInstancesPerMachine)
        .partitioned(numberOfInstances, this.numberOfBackups);
      
        return deployment;
    }
    
    protected int calcNumberOfPartitionsFromMemoryRequirements() {
        
        long maximumJavaHeapSizeMegabytes = 0;
        for (String arg : super.getVmInputArguments()) {
            String prefix = "-Xmx";
            if (arg.startsWith(prefix)) {
                maximumJavaHeapSizeMegabytes = MemoryUnit.toMegaBytes(arg.substring(prefix.length()));
                break;
            }
        }
                
        if (maximumJavaHeapSizeMegabytes == 0) {
            throw new IllegalStateException("-Xmx vmInputArgument is not defined.");    
        }
                
        int numberOfPartitions = (int) Math.floor(((double)maxMemoryCapacityMegabytes)/maximumJavaHeapSizeMegabytes);
        numberOfPartitions /= (numberOfBackups+1);
                
        return Math.max(1, numberOfPartitions);
    }
}