/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.admin.pu.topology;

import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.config.ProcessingUnitConfig;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependencies;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;

import com.gigaspaces.security.directory.UserDetails;

/**
 * @author itaif
 * @since 9.0.1
 */
public interface ProcessingUnitConfigHolder {

    /**
     * @see ProcessingUnitDeployment#secured(boolean)
     */
    void setSecured(Boolean secured);
    
    Boolean getSecured();
    
    /**
     * @see ProcessingUnitDeployment#userDetails(UserDetails)
     */
    void setUserDetails(UserDetailsConfig userDetails);
    
    UserDetailsConfig getUserDetails();

    /**
     * @see ProcessingUnitDeployment#setContextProperty(String, String)
     */
    void setContextProperties(Map<String,String> contextProperties);

    Map<String,String> getContextProperties();
    
    void setDependencies(ProcessingUnitDependencies<ProcessingUnitDependency> dependencies);

    String getName();
    
    /**
     * @see ProcessingUnitDeployment#name(String)
     */
    void setName(String name);
    
    public ProcessingUnitDependencies<ProcessingUnitDependency> getDependencies();
    
    ProcessingUnitConfig toProcessingUnitConfig();


}
