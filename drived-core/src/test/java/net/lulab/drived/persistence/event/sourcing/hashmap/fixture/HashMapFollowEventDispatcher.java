package net.lulab.drived.persistence.event.sourcing.hashmap.fixture;

import net.lulab.drived.event.sourcing.DispatchableDomainEvent;
import net.lulab.drived.event.sourcing.EventDispatcher;
import net.lulab.drived.event.sourcing.EventNotifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class HashMapFollowEventDispatcher implements EventDispatcher, EventNotifiable {

    private final Map<Long, HashMapStoredEvent> database;

    private final AtomicLong lastDispatchedId;

    private final List<EventDispatcher> dispatchers;

    public HashMapFollowEventDispatcher(Map<Long, HashMapStoredEvent> database) {
        this.database = database;
        this.lastDispatchedId = new AtomicLong(0L);
        this.dispatchers = new ArrayList<>();
    }

    @Override
    public void dispatch(DispatchableDomainEvent event) {
        for (EventDispatcher dispatcher : dispatchers) {
            if (dispatcher.isAbleDispatch(event)) {
                dispatcher.dispatch(event);
            }
        }
    }

    @Override
    public void registerDispatcher(EventDispatcher dispatcher) {
        this.dispatchers.add(dispatcher);
    }

    @Override
    public boolean isAbleDispatch(DispatchableDomainEvent event) {
        return true;
    }

    @Override
    public void notifyDispatchableDomainEvents() {
        long lastDispatchedId = findLastDispatchedId();

        List<HashMapStoredEvent> storedEvents = loadStoredEventAfter(lastDispatchedId);
        for (HashMapStoredEvent storedEvent : storedEvents) {
            dispatch(new DispatchableDomainEvent(
                    storedEvent.getStreamName(),
                    storedEvent.getVersion(),
                    storedEvent.getVersionedAt(),
                    storedEvent.getDomainEvent()));

            saveLastDispatchedId(storedEvent.getEventId());
        }
    }

    private void saveLastDispatchedId(long dispatchedId) {
        lastDispatchedId.set(dispatchedId);
    }

    private long findLastDispatchedId() {
        return lastDispatchedId.get();
    }

    private List<HashMapStoredEvent> loadStoredEventAfter(long lastDispatchedId) {
        long startEventId = lastDispatchedId + 1L;

        List<HashMapStoredEvent> storedEvents = new ArrayList<>();
        for (long eventId = startEventId; database.containsKey(eventId); eventId++) {
            HashMapStoredEvent storedEvent = database.get(eventId);
            storedEvents.add(storedEvent);
        }
        return storedEvents;
    }
}
