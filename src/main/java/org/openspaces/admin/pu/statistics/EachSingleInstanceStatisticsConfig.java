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
package org.openspaces.admin.pu.statistics;

import java.util.HashMap;
import java.util.Map;

/**
 * Denotes that a {@link ProcessingUnitStatisticsId} applies to all of the Processing Unit.
 * @author itaif
 * @since 9.0.0
 */
public class EachSingleInstanceStatisticsConfig 
    extends AbstractInstancesStatisticsConfig 
    implements InstancesStatisticsConfig {

    public EachSingleInstanceStatisticsConfig() {
        this(new HashMap<String,String>());
    }
    
    public EachSingleInstanceStatisticsConfig(Map<String,String> properties) {
        super(properties);
    }

    @Override
    public void validate() throws IllegalStateException {
        // ok
    }
}
