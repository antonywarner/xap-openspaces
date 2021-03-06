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
package org.openspaces.admin.internal.discovery;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gigaspaces.start.SystemInfo;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.DiscoveryLocatorManagement;
import net.jini.discovery.DiscoveryManagement;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.discovery.dynamic.DynamicLookupLocatorDiscovery;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.boot.BootUtil;
import org.jini.rio.resources.servicecore.Service;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.admin.AdminClosedException;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.esm.DefaultElasticServiceManager;
import org.openspaces.admin.internal.esm.InternalElasticServiceManager;
import org.openspaces.admin.internal.gsa.DefaultGridServiceAgent;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.internal.gsc.DefaultGridServiceContainer;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.gsm.DefaultGridServiceManager;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.lus.DefaultLookupService;
import org.openspaces.admin.internal.lus.InternalLookupService;
import org.openspaces.admin.internal.pu.DefaultProcessingUnitInstance;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.admin.internal.space.DefaultSpaceInstance;
import org.openspaces.admin.internal.space.InternalSpaceInstance;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.Space;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.grid.esm.ESM;
import org.openspaces.pu.container.servicegrid.PUDetails;
import org.openspaces.pu.container.servicegrid.PUServiceBean;
import org.openspaces.security.AdminFilterHelper;

import com.gigaspaces.grid.gsa.AgentIdAware;
import com.gigaspaces.grid.gsa.AgentProcessesDetails;
import com.gigaspaces.grid.gsa.GSA;
import com.gigaspaces.grid.gsc.GSC;
import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.grid.zone.GridZoneProvider;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMInfoProvider;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.utils.StringUtils;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.management.entry.JMXConnection;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.jini.SharedDiscoveryManagement;
import com.j_spaces.jmx.util.JMXUtilities;
import com.j_spaces.kernel.PlatformVersion;

/**
 * @author kimchy
 */
public class DiscoveryService implements DiscoveryListener, ServiceDiscoveryListener {

    private static final Log logger = LogFactory.getLog(DiscoveryService.class);

    private List<String> groups = null;

    private String locators = null;

    private final InternalAdmin admin;

    private boolean discoverUnmanagedSpaces = false;

    private volatile boolean started = false;
    
    private ServiceDiscoveryManager sdm;
    private LookupCache serviceCache;
    private LookupCache spaceCache;

    //types of services to discover
    private Class[] services = new Class[]{Service.class};
    
   
    public DiscoveryService(InternalAdmin admin) {
    	this.admin = admin;
    }

    public void setDiscoveryServices(Class...services)
    {
    	if(services == null || services.length == 0)
    		return ;

    	this.services = new Class[services.length];
    	for (int i = 0; i < services.length; i++) {
    		if(GridServiceAgent.class.isAssignableFrom(services[i]))
    			this.services[i] = GSA.class;
    		else if(GridServiceManager.class.isAssignableFrom(services[i]))
    			this.services[i] = GSM.class;
    		else if(GridServiceContainer.class.isAssignableFrom(services[i]))
    			this.services[i] = GSC.class;
    		else if(ElasticServiceManager.class.isAssignableFrom(services[i]))
    			this.services[i] = ESM.class;
    		else if(ProcessingUnit.class.isAssignableFrom(services[i]))
    			this.services[i] = PUServiceBean.class;
    		else if(Space.class.isAssignableFrom(services[i]))
    			this.services[i] = IJSpace.class;
    		else
    			throw new IllegalArgumentException("Illegal discovery type - " + services[i] );
    	}
    }
    
    public void addGroup(String group) {
        if (groups == null) {
            groups = new ArrayList<String>();
        }
        groups.add(group);
    }

    public void addLocator(String locator) {
        if (locators == null) {
            locators = locator;
        } else {
            locators += "," + locator;
        }
    }

    public void discoverUnmanagedSpaces() {
        this.discoverUnmanagedSpaces = true;
    }
    
    public void start() {
        if (started) {
            return;
        }
        started = true;
        try {
            sdm = SharedDiscoveryManagement.getServiceDiscoveryManager(getGroups(), getInitialLocators(), this);
        } catch (Exception e) {
            throw new AdminException("Failed to start discovery service, Service Discovery Manager failed to start", e);
        }

        try {
        	
            ServiceTemplate template = new ServiceTemplate(null, services , null);
            serviceCache = sdm.createLookupCache(template, null, this);
        } catch (Exception e) {
            sdm.terminate();
            throw new AdminException("Failed to start discovery service, Lookup Cache failed to start", e);
        }

        if (discoverUnmanagedSpaces) {
            try {
                ServiceTemplate template = new ServiceTemplate(null, new Class[] { IJSpace.class }, null);
                spaceCache = sdm.createLookupCache(template, null, this);
            } catch (Exception e) {
                serviceCache.terminate();
                sdm.terminate();
                throw new AdminException("Failed to start discovery service, Lookup Cache failed to start", e);
            }
        }
    }

	public void stop() {
        if (!started) {
            return;
        }
        started = false;
        serviceCache.terminate();
        if (spaceCache != null) {
            spaceCache.terminate();
        }
        sdm.terminate();
    }

    @Override
    public void discovered(final DiscoveryEvent disEvent) {
        for (final ServiceRegistrar registrar : disEvent.getRegistrars()) {
            final ServiceID serviceID = registrar.getServiceID();
			try {

                final JVMDetails jvmDetails = ((JVMInfoProvider) registrar.getRegistrar()).getJVMDetails();
                if( !AdminFilterHelper.acceptJvm( admin.getAdminFilter(), jvmDetails ) ){
                    continue;
                }
                
                final InternalLookupService lookupService = new DefaultLookupService(registrar,
                        serviceID, admin, ((AgentIdAware) registrar.getRegistrar()).getAgentId(),
                        ((AgentIdAware) registrar.getRegistrar()).getGSAServiceID(), jvmDetails);
                
                if (logger.isDebugEnabled()) {
                    logger.debug("Service Added [LUS] with uid [" + serviceID + "]");
                }
                
                final NIODetails nioDetails = lookupService.getNIODetails();
                final OSDetails osDetails = lookupService.getOSDetails();
                final String[] zones = ((GridZoneProvider) registrar.getRegistrar()).getZones();
                final Entry[] attributeSets = ((com.sun.jini.reggie.Registrar)registrar.getRegistrar()).getLookupAttributes();
                admin.scheduleNonBlockingStateChange(new Runnable() {
                    @Override
                    public void run() {
                        String jmxUrl = getJMXConnection( attributeSets );
                        admin.addLookupService(lookupService, nioDetails, osDetails, jvmDetails, jmxUrl, zones);
                        }
                });
        	} catch (AdminClosedException e) {
        		if (logger.isDebugEnabled()) {
                    logger.debug("Failed to add lookup service with id [" + serviceID + "] since admin is already closed", e);
                }
            } catch (Exception e) {
                logger.warn("Failed to add lookup service with id [" + serviceID + "]", e);
            }
        }

    }

    @Override
    public void discarded(final DiscoveryEvent e) {
        for (final ServiceRegistrar registrar : e.getRegistrars()) {
            final String serviceId = registrar.getServiceID().toString();
            try {
                admin.scheduleNonBlockingStateChange(new Runnable() {
                    @Override
                    public void run() {
                        admin.removeLookupService(serviceId);
                    }
                });
            } catch (AdminClosedException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to remove lookup service with id ["
                            + serviceId + "] since admin is already closed", ex);
                }
            }
        }
    }

    @Override
    public void serviceAdded(final ServiceDiscoveryEvent event) {
        final ServiceItem serviceItem = event.getPostEventServiceItem();
        final Object service = serviceItem.service;
        if (service instanceof GSM) {
            initGSM((GSM) service, serviceItem.serviceID, serviceItem);
        } else if (service instanceof ESM) {
            initESM((ESM) service, serviceItem.serviceID, serviceItem);
        } else if (service instanceof GSA) {
            initGSA((GSA) service, serviceItem.serviceID, serviceItem);
        } else if (service instanceof GSC) {
            initGSC((GSC) service, serviceItem.serviceID, serviceItem);
        } else if (service instanceof PUServiceBean) {
            initPU((PUServiceBean) service, serviceItem.serviceID, serviceItem);
        } else if (service instanceof IJSpace) {
            initSpaceProxy((ISpaceProxy) service, serviceItem.serviceID);
        }
    }
    
    private void initSpaceProxy(ISpaceProxy clusteredIjspace, ServiceID serviceID) {
        if (logger.isDebugEnabled()) {
            logger.debug("Service Added [Space Instance] with uid [" + serviceID + "]");
        }
        try {
            admin.login(clusteredIjspace);

            ISpaceProxy direcyIjspace = (ISpaceProxy)clusteredIjspace.getDirectProxy().getNonClusteredProxy();
            admin.login(direcyIjspace);

            final IInternalRemoteJSpaceAdmin spaceAdmin = (IInternalRemoteJSpaceAdmin) direcyIjspace.getAdmin();

            final JVMDetails jvmDetails = spaceAdmin.getJVMDetails(); 
            if( !AdminFilterHelper.acceptJvm( admin.getAdminFilter(), jvmDetails ) ){
                return;
            }

            final InternalSpaceInstance spaceInstance = new DefaultSpaceInstance( serviceID, 
                    direcyIjspace, spaceAdmin, admin, jvmDetails );

            final NIODetails nioDetails = spaceInstance.getNIODetails();
            final OSDetails osDetails = spaceInstance.getOSDetails();
            
            String jmxServiceURL = null;
            try{
                jmxServiceURL = JMXUtilities.createJMXUrl(direcyIjspace.getDirectProxy().getContainerConfig().jndiUrl);
            }
            catch( RemoteException re ){
                logger.warn( "Failed to fetch jndi url from space container", re);   
            }
            final String optionalJmxServiceURL = jmxServiceURL;
            
            final String[] zones = spaceAdmin.getZones();
            
            admin.scheduleNonBlockingStateChange(new Runnable() {
                @Override
                public void run() {
                    admin.addSpaceInstance(spaceInstance, nioDetails, osDetails, jvmDetails, optionalJmxServiceURL, zones);
                }
            });
        } catch (AdminClosedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to add Space instance since admin is already closed", e);
            }
        }
        catch (Exception e) {
            logger.warn("Failed to add [Space Instance] with uid [" + serviceID + "]", e);
        }
    }

    private void initPU(final PUServiceBean puServiceBean, ServiceID serviceID, final ServiceItem serviceItem) {
        if (logger.isDebugEnabled()) {
            logger.debug("Service Added [Processing Unit Instance] with uid [" + serviceID + "]");
        }
        try {
            PUDetails puDetails = puServiceBean.getPUDetails();
            if (puDetails == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to add [Processing Unit Instance] with uid [" + serviceID + "] since instance is being shutdown");
                }
            }
            else {
                final JVMDetails jvmDetails = puServiceBean.getJVMDetails();                
                if( !AdminFilterHelper.acceptJvm( admin.getAdminFilter(), jvmDetails ) ){
                    return;
                }                

                final InternalProcessingUnitInstance processingUnitInstance = new DefaultProcessingUnitInstance(
                        serviceID, puDetails, puServiceBean, admin, jvmDetails);

                final NIODetails nioDetails = processingUnitInstance.getNIODetails();
                final OSDetails osDetails = processingUnitInstance.getOSDetails();

                final String[] zones = puServiceBean.getZones();
                admin.scheduleNonBlockingStateChange(new Runnable() {
                    @Override
                    public void run() {
                        String jmxUrl = getJMXConnection( serviceItem.attributeSets );
                        admin.addProcessingUnitInstance(processingUnitInstance, nioDetails, osDetails, jvmDetails, jmxUrl,
                                zones);
                        if (!discoverUnmanagedSpaces) {
                            for (SpaceServiceDetails serviceDetails : processingUnitInstance.getEmbeddedSpacesDetails()) {
                                InternalSpaceInstance spaceInstance = new DefaultSpaceInstance(puServiceBean,
                                        serviceDetails, admin, jvmDetails );
                                admin.addSpaceInstance(spaceInstance, nioDetails, osDetails, jvmDetails, jmxUrl, zones);
                            }
                        }
                    }
                });
            }
        } catch (AdminClosedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to add [Processing Unit Instance] with uid " + serviceID + " since admin is already closed", e);
            }
        } catch (Exception e) {
            logger.warn("Failed to add [Processing Unit Instance] with uid [" + serviceID + "]", e);
        }
    }

    private void initGSC(GSC gsc, ServiceID serviceID, final ServiceItem serviceItem) {
        if (logger.isDebugEnabled()) {
            logger.debug("Service Added [GSC] with uid [" + serviceID + "]");
        }
        try {
            
            final JVMDetails jvmDetails = gsc.getJVMDetails();
            if( !AdminFilterHelper.acceptJvm( admin.getAdminFilter(), jvmDetails ) ){
                return;
            }
            
            admin.login(gsc);
            
            final InternalGridServiceContainer gridServiceContainer = new DefaultGridServiceContainer(
                    serviceID, gsc, admin, gsc.getAgentId(), gsc.getGSAServiceID(), jvmDetails);
            
            // get the details here, on the thread pool
            final NIODetails nioDetails = gridServiceContainer.getNIODetails();
            final OSDetails osDetails = gridServiceContainer.getOSDetails();
            
            final String[] zones = gsc.getZones();
            admin.scheduleNonBlockingStateChange(new Runnable() {
                @Override
                public void run() {
                    String jmxUrl = getJMXConnection( serviceItem.attributeSets );
                    admin.addGridServiceContainer(gridServiceContainer, nioDetails, 
                            osDetails, jvmDetails, jmxUrl, zones);
                }
            });
        } catch (AdminClosedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to add GSC since admin is already closed", e);
            }
        } catch (Exception e) {
            logger.warn("Failed to add GSC with uid [" + serviceID + "]", e);
        }
    }

    private void initGSA(GSA gsa, ServiceID serviceID, final ServiceItem serviceItem) {
        if (logger.isDebugEnabled()) {
            logger.debug("Service Added [GSA] with uid [" + serviceID + "]");
        }
        try {
            final JVMDetails jvmDetails = gsa.getJVMDetails();
            if( !AdminFilterHelper.acceptJvm( admin.getAdminFilter(), jvmDetails ) ){
                return;
            }            
            
            admin.login(gsa);
            
            AgentProcessesDetails processesDetails = gsa.getDetails();
            final InternalGridServiceAgent gridServiceAgent = new DefaultGridServiceAgent(serviceID, 
                    gsa, admin, processesDetails, jvmDetails);
            

            // get the details here, on the thread pool
            final NIODetails nioDetails = gridServiceAgent.getNIODetails();
            final OSDetails osDetails = gridServiceAgent.getOSDetails();
            final String[] zones = gsa.getZones();
            admin.scheduleNonBlockingStateChange(new Runnable() {
                @Override
                public void run() {
                    String jmxUrl = getJMXConnection( serviceItem.attributeSets );
                    admin.addGridServiceAgent(gridServiceAgent, nioDetails, osDetails, jvmDetails, jmxUrl, zones);
                }
            });
        } catch (AdminClosedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to add GSA since admin is already closed", e);
            }
        
        } catch (Exception e) {
            logger.warn("Failed to add [GSA] with uid [" + serviceID + "]", e);
        }
    }

    private void initESM(ESM esm, ServiceID serviceID, final ServiceItem serviceItem) {
        if (logger.isDebugEnabled()) {
            logger.debug("Service Added [ESM] with uid [" + serviceID + "]");
        }
        try {
            admin.login(esm);
            final JVMDetails jvmDetails = esm.getJVMDetails();
            final InternalElasticServiceManager elasticServiceManager = 
                    new DefaultElasticServiceManager(serviceID, esm, admin, esm.getAgentId(), 
                            esm.getGSAServiceID(), jvmDetails );
            // get the details here, on the thread pool
            final NIODetails nioDetails = elasticServiceManager.getNIODetails();
            final OSDetails osDetails = elasticServiceManager.getOSDetails();
            
            final String[] zones = esm.getZones();
            admin.scheduleNonBlockingStateChange(new Runnable() {
                @Override
                public void run() {
                    String jmxUrl = getJMXConnection( serviceItem.attributeSets );
                    boolean acceptVM = AdminFilterHelper.acceptJvm( admin.getAdminFilter(), jvmDetails );
                    admin.addElasticServiceManager(elasticServiceManager, nioDetails, 
                                                osDetails, jvmDetails, jmxUrl, zones, acceptVM);
                }
            });
        } catch (AdminClosedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to add ESM since admin is already closed", e);
            }
        } catch (Exception e) {
            logger.warn("Failed to add [ESM] with uid [" + serviceID + "]", e);
        }        
    }

    private void initGSM(GSM gsm, ServiceID serviceID, final ServiceItem serviceItem) {
        if (logger.isDebugEnabled()) {
            logger.debug("Service Added [GSM] with uid [" + serviceID + "]");
        }
        try {
            admin.login(gsm);

            final JVMDetails jvmDetails = gsm.getJVMDetails();
            final InternalGridServiceManager gridServiceManager = new DefaultGridServiceManager(serviceID, gsm,
                    admin, gsm.getAgentId(), gsm.getGSAServiceID(), jvmDetails);
            
            
            // get the details here, on the thread pool
            final NIODetails nioDetails = gridServiceManager.getNIODetails();
            final OSDetails osDetails = gridServiceManager.getOSDetails();
            
            final String[] zones = gsm.getZones();
            admin.scheduleNonBlockingStateChange(new Runnable() {
                @Override
                public void run() {
                    String jmxUrl = getJMXConnection( serviceItem.attributeSets );
                    boolean acceptVM = AdminFilterHelper.acceptJvm( admin.getAdminFilter(), jvmDetails );
                    admin.addGridServiceManager(gridServiceManager, nioDetails, osDetails, 
                                                jvmDetails, jmxUrl, zones, acceptVM);
                }
            });
        } catch (AdminClosedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to add GSM since admin is already closed", e);
            }
        } catch (Exception e) {
            logger.warn("Failed to add [GSM] with uid [" + serviceID + "]", e);
        }        
    }
    
    /**
     * Get the String value found in the JMXConnection entry, or null if the attribute
     * set does not include a JMXConnection
     */
    public static String getJMXConnection( Entry[] attrs ) 
    {
        String jmxConn = null;
        for( int x = 0; x < attrs.length; x++ ) 
        {
            if( attrs[ x ] instanceof JMXConnection ) 
            {
                jmxConn = ( ( JMXConnection )attrs[ x ] ).jmxServiceURL;
                break;
            }
        }
        
        return jmxConn;
    }  

    @Override
    public void serviceRemoved(final ServiceDiscoveryEvent event) {
    	final Object service = event.getPreEventServiceItem().service;
    	final ServiceID serviceID = event.getPreEventServiceItem().serviceID;
    	try {
    	admin.scheduleNonBlockingStateChange(new Runnable() {
            @Override
            public void run() {
                if (service instanceof GSM) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Service Removed [GSM] with uid [" + serviceID + "]");
                    }
                    admin.removeGridServiceManager(serviceID.toString());
                } else if (service instanceof ESM) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Service Removed [ESM] with uid [" + serviceID + "]");
                    }
                    admin.removeElasticServiceManager(serviceID.toString());
                } else if (service instanceof GSA) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Service Removed [GSA] with uid [" + serviceID + "]");
                    }
                    admin.removeGridServiceAgent(serviceID.toString());
                } else if (service instanceof GSC) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Service Removed [GSC] with uid [" + serviceID + "]");
                    }
                    admin.removeGridServiceContainer(serviceID.toString());
                } else if (service instanceof PUServiceBean) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Service Removed [Processing Unit Instance] with uid [" + serviceID + "]");
                    }
                    admin.removeProcessingUnitInstance(serviceID.toString(), !discoverUnmanagedSpaces);
                } else if (service instanceof IJSpace) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Service Removed [Space Instance] with uid [" + serviceID + "]");
                    }
                    admin.removeSpaceInstance(serviceID.toString());
                }
            }
        });
        } catch (AdminClosedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Failed to remove service " + service.getClass()
                                + " id " + serviceID
                                + " since admin is already closed", e);
            }
        }
    }

    @Override
    public void serviceChanged(ServiceDiscoveryEvent event) {
        // TODO do we really care about this?
    }

    public String[] getGroups() {
        String[] groups;
        if (this.groups == null) {
            String groupsProperty = SystemInfo.singleton().lookup().groups();
            if (groupsProperty != null) {
            	groups = StringUtils.commaDelimitedListToStringArray( groupsProperty );
            } else {
                groups = new String[] { SystemInfo.singleton().lookup().defaultGroups() };
            }
        } else {
            groups = this.groups.toArray(new String[this.groups.size()]);
        }
        return groups;
    }

    public LookupLocator[] getLocators() {
        LookupLocator[] result;
        if (!isDynamicLocatorsEnabled()) {
            result = getInitialLocators();
        } else {
            result = getDynamicLocators();
        }
        return result;
    }
    
    public boolean isDynamicLocatorsEnabled()
    {
        if (started && sdm != null && sdm.getDiscoveryManager() instanceof LookupDiscoveryManager) {
            LookupDiscoveryManager ldm = (LookupDiscoveryManager) sdm.getDiscoveryManager();
            return DynamicLookupLocatorDiscovery.dynamicLocatorsEnabled() || 
                   ldm.getDynamicLocatorDiscovery().isInitialized();
        }
        
        return false;
    }
    
    /**
     * @return If the service discovery manager is using initial locators
     *         of lookup services with dynamic locators enabled, this will return
     *         the currently discovered locators, otherwise, it will return the initial locators.
     */
    private LookupLocator[] getDynamicLocators() {
        try {
            return ((DiscoveryLocatorManagement)sdm.getDiscoveryManager()).getLocators();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed retrieving dynamic locators from admin, returning initial locators", e);
            }
            return getInitialLocators();
        }
    }
    
    private LookupLocator[] getInitialLocators() {
        if (locators == null) {
            String locatorsProperty = SystemInfo.singleton().lookup().locators();
            if (locatorsProperty != null) {
                locators = locatorsProperty;
            }
        }
        return BootUtil.toLookupLocators(locators);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean verbose) {
        DiscoveryManagement ldm = sdm.getDiscoveryManager();
        String message = super.toString() +
                " sdm=" + Integer.toHexString(sdm.hashCode()) +
                " ldm=" + Integer.toHexString(ldm.hashCode());
        if (verbose)
            message += " groups=" + Arrays.toString(getGroups()) + " locators=" + Arrays.toString(getLocators());
        return message;
    }
}
