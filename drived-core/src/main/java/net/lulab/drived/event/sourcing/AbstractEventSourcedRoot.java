package net.lulab.drived.event.sourcing;

import net.lulab.drived.domain.model.AggregateRoot;
import net.lulab.drived.event.DomainEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class AbstractEventSourcedRoot implements AggregateRoot {

    private long version;

    private final List<DomainEvent> pendingEvents;

    private transient Map<Class, Consumer<? extends DomainEvent>> mutationMap;

    protected AbstractEventSourcedRoot() {
        this.version = 0L;
        this.pendingEvents = new CopyOnWriteArrayList<>();
        initializeMutationMap();
    }

    protected abstract void initializeMutationMap();

    protected <T extends DomainEvent> void addMutate(Class<T> eventClass,
                                                     Consumer<T> mutator) {
        if (mutationMap == null) {
            mutationMap = new HashMap<>();
        }
        mutationMap.put(eventClass, mutator);
    }

    public long getVersion() {
        return version;
    }

    public List<DomainEvent> getPendingEvents() {
        return pendingEvents;
    }

    public void commitEvents(List<DomainEvent> pendingEvents) {
        this.pendingEvents.removeAll(pendingEvents);
    }

    protected void replay(List<DomainEvent> events, long version) {
        for (DomainEvent event : events) {
            this.apply(event);
        }
        this.version = version;
    }

    protected void raiseEvent(DomainEvent event) {
        pendingEvents.add(event);
        apply(event);
    }

    @SuppressWarnings("unchecked")
    private void apply(DomainEvent event) {
        Class<? extends DomainEvent> eventClass = event.getClass();
        Consumer consumer = mutationMap.get(eventClass);
        if (consumer != null) {
            consumer.accept(event);
            this.version++;
        }
    }
}
