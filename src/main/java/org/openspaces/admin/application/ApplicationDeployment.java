/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.application;

import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigHolder;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;

/**
 * Describes an application deployment that consists of one or more processing unit deployments.
 * @since 8.0.6
 * @author itaif
 */
public class ApplicationDeployment {

    private final ApplicationConfig config;
    
    /**
     * Creates a new application deployment with the specified name
     */
    public ApplicationDeployment(String applicationName) {
        config = new ApplicationConfig();
        config.setName(applicationName);
    }
    
    /**
     * Creates a new application deployment with the specified name, and pu deployments
     */
    public ApplicationDeployment(String applicationName, ProcessingUnitDeploymentTopology ... processingUnitDeployments) {
        this(applicationName);
        for (ProcessingUnitDeploymentTopology puDeployment : processingUnitDeployments) {
            addProcessingUnitDeployment(puDeployment);
        }
    }
    
    
    /**
     * Creates a new application deployment with the specified name, and pu deployments
     */
    public ApplicationDeployment(String applicationName, ProcessingUnitConfigHolder... processingUnitConfigHolders) {
        this(applicationName);
        for (ProcessingUnitConfigHolder puConfigHolder : processingUnitConfigHolders) {
            addProcessingUnitDeployment(puConfigHolder);
        }
    }

    /**
     * @param config created by derived class
     */
    protected ApplicationDeployment(ApplicationConfig config) {
        this.config = config;
    }

    /**
     * Deprecated Method. Use {@link #addProcessingUnitDeployment(ProcessingUnitDeploymentTopology)} instead
     */
    @Deprecated
    public ApplicationDeployment deployProcessingUnit(ProcessingUnitDeploymentTopology puDeployment) {
        return addProcessingUnitDeployment(puDeployment);
    }

    /**
     * Adds a processing unit deployment to this application deployment.
     * All processing units are deployed in parallel (unless dependencies are defined)
     */
    public ApplicationDeployment addProcessingUnitDeployment(ProcessingUnitDeploymentTopology puDeployment) {
        addProcessingUnitDeployment(puDeployment.create());
        return this;
    }
    
    public ApplicationDeployment addProcessingUnitDeployment(ProcessingUnitConfigHolder puConfigHolder) {
        config.addProcessingUnit(puConfigHolder);
        return this;
    }
    
    public ApplicationConfig create() {
        return config;
    }
}
