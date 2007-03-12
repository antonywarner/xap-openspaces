package org.openspaces.jdbc.config;

import org.openspaces.jdbc.datasource.SpaceDriverManagerDataSource;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class DataSourceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return SpaceDriverManagerDataSource.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String space = element.getAttribute("space");
        builder.addPropertyReference("space", space);
    }
}
