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

package org.openspaces.persistency.hibernate;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.ManagedDataSource;
import com.gigaspaces.datasource.hibernate.SessionFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.openspaces.persistency.hibernate.iterator.HibernateProxyRemoverIterator;
import org.openspaces.persistency.patterns.ManagedDataSourceEntriesProvider;
import org.openspaces.persistency.support.ConcurrentMultiDataIterator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author kimchy
 */
public abstract class AbstractHibernateExternalDataSource implements ManagedDataSource, ManagedDataSourceEntriesProvider {

    protected final Log logger = LogFactory.getLog(getClass());

    public static final String HIBERNATE_CFG_PROPERTY = "hibernate-config-file";

    private SessionFactory sessionFactory;

    private Set<String> managedEntries;

    private String[] initialLoadEntries;

    private int fetchSize = 100;

    private int initialLoadThreadPoolSize = 10;

    private int initalLoadChunkSize = 100000;

    private boolean performOrderById = true;

    private boolean createdSessionFactory = true;

    /**
     * Injects the Hibernate SessionFactory to be used with this external data source.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        createdSessionFactory = false;
        this.sessionFactory = sessionFactory;
    }

    /**
     * Returns the Hibernate session factory.
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Sets all the entries this Hibernate data source will work with. By default, will use Hiberante meta
     * data API in order to get the list of all the given entities it handles.
     *
     * <p>This list is used to filter out entities when performing all data source operaions exception for
     * the {@link #initialLoad()} operation.
     *
     * <p>Usually, there is no need to explicitly set this.
     */
    public void setManagedEntries(String... entries) {
        this.managedEntries = new HashSet<String>();
        this.managedEntries.addAll(Arrays.asList(entries));
    }

    /**
     * Returns all the entries this Hibernate data source will work with. By default, will use Hiberante meta
     * data API in order to get the list of all the given entities it handles.
     *
     * <p>This list is used to filter out entities when performing all data source operaions exception for
     * the {@link #initialLoad()} operation.
     */
    public String[] getManagedEntries() {
        return managedEntries.toArray(new String[managedEntries.size()]);
    }

    /**
     * Returns if the given entity name is part of the {@link #getManagedEntries()} list.
     */
    protected boolean isManagedEntry(String entityName) {
        return managedEntries.contains(entityName);
    }

    /**
     * Sets the fetch size that will be used when working with scrollable results. Defauls to
     * <code>100</code>.
     *
     * @see org.hibernate.Criteria#setFetchSize(int)
     */
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    /**
     * Returns the fetch size that will be used when working with scrollable results. Defauls to
     * <code>100</code>.
     *
     * @see org.hibernate.Criteria#setFetchSize(int)
     */
    protected int getFetchSize() {
        return fetchSize;
    }

    /**
     * When performing intial load, this flag indicates if the generated query will order to results by
     * the id. By default set to <code>true</code> as it most times results in better inital load performance.
     */
    public void setPerformOrderById(boolean performOrderById) {
        this.performOrderById = performOrderById;
    }

    /**
     * When performing intial load, this flag indicates if the generated query will order to results by
     * the id. By default set to <code>true</code> as it most times results in better inital load performance.
     */
    protected boolean isPerformOrderById() {
        return performOrderById;
    }

    /**
     * Sets a list of entries that will be used to perform the {@link #initialLoad()} operation. By default, will
     * try and build a sensible list basde on Hiberante meta data.
     *
     * <p>Note, sometimes an explicit list should be provided. For example, if we have a class A and class B, and
     * A has a relationship to B which is not component. If in the space, we only wish to have A, and have B just
     * as a field in A (and not as an Entry), then we need to explciitly set the list just to A. By default, if
     * we won't set it, it will result in two entries existing in the Space, A and B, with A having a field of B
     * as well.
     */
    public void setInitialLoadEntries(String... initialLoadEntries) {
        this.initialLoadEntries = initialLoadEntries;
    }

    /**
     * Returns a list of entries that will be used to perform the {@link #initialLoad()} operation. By default, will
     * try and build a sensible list basde on Hiberante meta data.
     *
     * <p>Note, sometimes an explicit list should be provided. For example, if we have a class A and class B, and
     * A has a relationship to B which is not component. If in the space, we only wish to have A, and have B just
     * as a field in A (and not as an Entry), then we need to explciitly set the list just to A. By default, if
     * we won't set it, it will result in two entries existing in the Space, A and B, with A having a field of B
     * as well.
     */
    public String[] getInitialLoadEntries() {
        return initialLoadEntries;
    }

    /**
     * The inital load operation uses the {@link org.openspaces.persistency.support.ConcurrentMultiDataIterator}.
     * This property allows to control the thread pool size of the concurrent multi data iterator. Defaults to
     * <code>10</code>.
     *
     * <p>Note, this usually will map one to one to the number of open connections / cursors against the database.
     */
    public void setInitialLoadThreadPoolSize(int initialLoadThreadPoolSize) {
        this.initialLoadThreadPoolSize = initialLoadThreadPoolSize;
    }

    /**
     * By default, the inital load process will chunk large tables and will iterate over the table (entity) per
     * chunk (concurrently). This setting allows to control the chunk size to split the table by. By default, set
     * to <code>100,000</code>. Batching can be disabled by setting <code>-1</code>.
     */
    public void setInitalLoadChunkSize(int initalLoadChunkSize) {
        this.initalLoadChunkSize = initalLoadChunkSize;
    }

    /**
     * By default, the inital load process will chunk large tables and will iterate over the table (entity) per
     * chunk (concurrently). This setting allows to control the chunk size to split the table by. By default, set
     * to <code>100,000</code>. Batching can be disabled by setting <code>-1</code>.
     */
    protected int getInitalLoadChunkSize() {
        return initalLoadChunkSize;
    }

    public void init(Properties properties) throws DataSourceException {
        if (sessionFactory == null) {
            createdSessionFactory = true;
            String hibernateFile = properties.getProperty(HIBERNATE_CFG_PROPERTY);
            if (hibernateFile == null) {
                logger.error("No session factory injected, and [" + HIBERNATE_CFG_PROPERTY + "] is not provided in the properties file, can't create session factory");
            }
            try {
                sessionFactory = SessionFactoryBuilder.getFactory(hibernateFile);
            } catch (Exception e) {
                throw new DataSourceException("Failed to create session factory from properties file [" + hibernateFile + "]");
            }
        }
        if (managedEntries == null) {
            managedEntries = new HashSet<String>();
            // try and derive the managedEntries
            Map allClassMetaData = sessionFactory.getAllClassMetadata();
            for (Iterator it = allClassMetaData.keySet().iterator(); it.hasNext();) {
                String entityname = (String) it.next();
                managedEntries.add(entityname);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Using Hibernate managedEntries [" + managedEntries + "]");
        }
        if (initialLoadEntries == null) {
            Set<String> initialLoadEntries = new HashSet<String>();
            // try and derive the managedEntries
            Map allClassMetaData = sessionFactory.getAllClassMetadata();
            for (Iterator it = allClassMetaData.keySet().iterator(); it.hasNext();) {
                String entityname = (String) it.next();
                ClassMetadata classMetadata = (ClassMetadata) allClassMetaData.get(entityname);
                if (classMetadata.isInherited()) {
                    String superClassEntityName = ((AbstractEntityPersister) classMetadata).getMappedSuperclass();
                    ClassMetadata superClassMetadata = (ClassMetadata) allClassMetaData.get(superClassEntityName);
                    Class superClass = superClassMetadata.getMappedClass(EntityMode.POJO);
                    // only filter out classes that their super class has compass mappings
                    if (superClass != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Entity [" + entityname + "] is inherited and has a super class ["
                                    + superClass + "] filtering it out for intial load managedEntries");
                        }
                        continue;
                    }
                }
                initialLoadEntries.add(entityname);
            }
            this.initialLoadEntries = initialLoadEntries.toArray(new String[initialLoadEntries.size()]);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Using Hibernate inital load managedEntries [" + Arrays.toString(initialLoadEntries) + "]");
        }
    }

    public void shutdown() throws DataSourceException {
        if (createdSessionFactory) {
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                try {
                    sessionFactory.close();
                } finally {
                    sessionFactory = null;
                }
            }
        }
    }

    /**
     * A helper method that creates the initial load itertaor using the {@link org.openspaces.persistency.support.ConcurrentMultiDataIterator}
     * with the provided {@link #setInitialLoadThreadPoolSize(int)} thread pool size.
     */
    protected DataIterator createInitialLoadIterator(DataIterator[] iterators) {
        return new HibernateProxyRemoverIterator(new ConcurrentMultiDataIterator(iterators, initialLoadThreadPoolSize));
    }

}
