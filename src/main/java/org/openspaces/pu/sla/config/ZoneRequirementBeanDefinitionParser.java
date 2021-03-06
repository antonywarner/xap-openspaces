/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.pu.sla.config;

import org.openspaces.pu.sla.requirement.ZoneRequirement;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link org.openspaces.pu.sla.requirement.ZoneRequirement}.
 *
 * @author kimchy
 */
public class ZoneRequirementBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class<ZoneRequirement> getBeanClass(Element element) {
        return ZoneRequirement.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String zone = element.getAttribute("name");
        builder.addPropertyValue("zone", zone);
    }
}