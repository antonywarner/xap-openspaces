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

import java.util.concurrent.TimeUnit;
/**
 * Fluent API for creating a new {@link ThroughputTimeWindowStatisticsConfig} object
 * @author itaif, gal
 * @since 9.0.0
 */
public class ThroughputTimeWindowStatisticsConfigurer {

    private ThroughputTimeWindowStatisticsConfig config = new ThroughputTimeWindowStatisticsConfig();
    
    public ThroughputTimeWindowStatisticsConfigurer timeWindow(long timeWindow, TimeUnit timeUnit) {
        config.setTimeWindowSeconds(timeUnit.toSeconds(timeWindow));
        return this;
    }
    
    public ThroughputTimeWindowStatisticsConfigurer minimumTimeWindow(long timeWindow, TimeUnit timeUnit) {
        config.setMinimumTimeWindowSeconds(timeUnit.toSeconds(timeWindow));
        return this;
    }
    
    public ThroughputTimeWindowStatisticsConfigurer maximumTimeWindow(long timeWindow, TimeUnit timeUnit) {
        config.setMaximumTimeWindowSeconds(timeUnit.toSeconds(timeWindow));
        return this;
    }
        
    public ThroughputTimeWindowStatisticsConfig create() {
        config.validate();
        return config;
    }

}
