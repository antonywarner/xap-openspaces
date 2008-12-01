package org.openspaces.admin.internal.lus.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.lus.InternalLookupServices;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.events.LookupServiceAddedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultLookupServiceAddedEventManager implements InternalLookupServiceAddedEventManager {

    private final InternalLookupServices lookupServices;

    private final InternalAdmin admin;

    private final List<LookupServiceAddedEventListener> listeners = new CopyOnWriteArrayList<LookupServiceAddedEventListener>();

    public DefaultLookupServiceAddedEventManager(InternalLookupServices lookupServices) {
        this.lookupServices = lookupServices;
        this.admin = (InternalAdmin) lookupServices.getAdmin();
    }

    public void lookupServiceAdded(final LookupService lookupService) {
        for (final LookupServiceAddedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.lookupServiceAdded(lookupService);
                }
            });
        }
    }

    public void add(final LookupServiceAddedEventListener eventListener) {
        admin.raiseEvent(eventListener, new Runnable() {
            public void run() {
                for (LookupService lookupService : lookupServices) {
                    eventListener.lookupServiceAdded(lookupService);
                }
            }
        });
        listeners.add(eventListener);
    }

    public void remove(LookupServiceAddedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureLookupServiceAddedEventListener(eventListener));
        } else {
            add((LookupServiceAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureLookupServiceAddedEventListener(eventListener));
        } else {
            remove((LookupServiceAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}