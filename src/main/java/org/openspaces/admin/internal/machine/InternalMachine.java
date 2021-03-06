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
package org.openspaces.admin.internal.machine;

import org.openspaces.admin.internal.pu.InternalProcessingUnitInstancesAware;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.SpaceInstance;

/**
 * @author kimchy
 */
public interface InternalMachine extends Machine, InternalProcessingUnitInstancesAware {

    void setOperatingSystem(OperatingSystem operatingSystem);

    void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance);

    void removeProcessingUnitInstance(String uid);

    void addSpaceInstance(SpaceInstance spaceInstance);

    void removeSpaceInstance(String uid);
}
