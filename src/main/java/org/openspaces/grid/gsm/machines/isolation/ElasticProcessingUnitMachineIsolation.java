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
package org.openspaces.grid.gsm.machines.isolation;

public abstract class ElasticProcessingUnitMachineIsolation {
    
    /**
     * @return non-unique name that is specific to this isolation type.
     */
    public abstract String getName();
    
    /**
     * @return true if this processing unit can be deployed on the same machine as the specified processing unit deployment. 
     */
    public abstract boolean equals(Object otherProcessingUnitIsolation);
    
    public abstract int hashCode();
    
    /**
     * @return a unique identifier for this machine isolation
     */
    public abstract String toString();
}
