package org.openspaces.example.data.feeder;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.example.data.common.Data;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A feeder bean started a scheduled task that writes a new Data object to the space.
 *
 * <p>The space is injected into this bean using OpenSpaces support for @GigaSpaceContext
 * annoation.
 *
 * <p>The scheduled support uses the java.util.concurrent Scheduled Executor Service. It
 * is started and stopped based on Spring lifeceycle events.
 *
 * @author kimchy
 */
public class DataFeeder implements InitializingBean, DisposableBean {

    private ScheduledExecutorService executorService;

    private ScheduledFuture<?> sf;

    private long defaultDelay = 1000;

    private DataFeederTask dataFeederTask;

    @GigaSpaceContext(name = "gigaSpace")
    private GigaSpace gigaSpace;

    public void setDefaultDelay(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    public void afterPropertiesSet() throws Exception {
        System.out.println("--- STARTING FEEDER WITH CYCLE [" + defaultDelay + "]");
        executorService = Executors.newScheduledThreadPool(1);
        dataFeederTask = new DataFeederTask();
        sf = executorService.scheduleAtFixedRate(dataFeederTask, defaultDelay, defaultDelay,
                TimeUnit.MILLISECONDS);
    }

    public void destroy() throws Exception {
        sf.cancel(true);
        sf = null;
        executorService.shutdown();
    }

    public class DataFeederTask implements Runnable {

        private int counter;

        public void run() {
            try {
                long time = System.currentTimeMillis();
                Data data = new Data(Data.TYPES[counter++ % Data.TYPES.length], "FEEDER " + Long.toString(time));
                data.setProcessed(false);
                gigaSpace.write(data);
                System.out.println("--- WROTE " + data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public int getCounter() {
            return counter;
        }
    }

    public int getFeedCount() {
        return dataFeederTask.getCounter();
    }
}
