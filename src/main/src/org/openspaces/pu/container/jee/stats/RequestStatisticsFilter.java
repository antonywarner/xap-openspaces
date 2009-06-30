package org.openspaces.pu.container.jee.stats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.boot.SharedServiceData;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.container.jee.JeeProcessingUnitContainerProvider;
import org.openspaces.pu.container.jee.JeeServiceDetails;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.pu.service.ServiceMonitorsProvider;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A simple statistics filter that counts the total processed and failed (i.e. resulted in an exception) requests,
 * as well as the number of active requests.
 *
 * @author kimchy
 */
public class RequestStatisticsFilter implements Filter, ServiceMonitorsProvider {

    private static Log logger = LogFactory.getLog(RequestStatisticsFilter.class);

    private AtomicLong requests;

    private AtomicLong reqeustsActive;
    private AtomicLong requestsDurationTotal;

    public void init(FilterConfig filterConfig) throws ServletException {
        requests = new AtomicLong();
        reqeustsActive = new AtomicLong();
        requestsDurationTotal = new AtomicLong();

        ClusterInfo clusterInfo = (ClusterInfo) filterConfig.getServletContext().getAttribute(JeeProcessingUnitContainerProvider.CLUSTER_INFO_CONTEXT);
        if (clusterInfo == null) {
            logger.warn("Failed to find cluster info, can't register JEE request service monitor");
        } else {
            SharedServiceData.addServiceMonitors(clusterInfo.getName() + clusterInfo.getRunningNumber(), new Callable() {
                public Object call() throws Exception {
                    return RequestStatisticsFilter.this.getServicesMonitors();
                }
            });
        }
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long now = System.currentTimeMillis();
        try {
            reqeustsActive.incrementAndGet();
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            reqeustsActive.decrementAndGet();
            long requestDuration = System.currentTimeMillis() - now;
            requestsDurationTotal.getAndAdd(requestDuration);
            requests.incrementAndGet();
        }
    }

    public void destroy() {
    }

    public ServiceMonitors[] getServicesMonitors() {
        return new ServiceMonitors[]{new WebRequestsServiceMonitors(JeeServiceDetails.ID, requests.get(), reqeustsActive.get(), requestsDurationTotal.get())};
    }
}
