package net.lulab.drived.event.sourcing;

import net.lulab.drived.domain.model.AggregateId;
import net.lulab.drived.domain.model.AggregateRoot;
import net.lulab.drived.domain.model.AggregateStore;
import net.lulab.drived.event.DomainEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class EventSourcedAggregateStore implements AggregateStore {

    private final EventStore eventStore;

    private final SnapshotStore snapshotStore;

    public EventSourcedAggregateStore(EventStore eventStore,
                                      SnapshotStore snapshotStore) {
        this.eventStore = eventStore;
        this.snapshotStore = snapshotStore;
    }

    @Override
    public <T extends AggregateRoot> T load(AggregateId id,
                                            Class<T> aggregateType) {

        EventStreamId eventStreamId = new EventStreamId(id.getId());
        AbstractEventSourcedRoot aggregate = restoreFromSnapshot(aggregateType, eventStreamId);

        if (aggregate == null) {
            return replaySinceFirstVersioned(eventStreamId, aggregateType);
        } else {
            return replaySinceLastVersioned(eventStreamId, aggregate);
        }
    }

    private <T extends AggregateRoot> T replaySinceFirstVersioned(EventStreamId eventStreamId,
                                                                  Class<T> aggregateType) {
        try {
            EventStream eventStream = eventStore.loadEventStreamSince(eventStreamId);
            Constructor<T> constructor = aggregateType.getDeclaredConstructor(
                    List.class,
                    long.class);
            return constructor.newInstance(
                    eventStream.getEvents(),
                    eventStream.getVersion());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "EventSourcedAggregateRoot should be have constructor" +
                            " with `Events, Version`",
                    e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(
                    "EventSourcedAggregateRoot may be not create an new instance",
                    e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends AggregateRoot> T replaySinceLastVersioned(EventStreamId eventStreamId,
                                                                 AbstractEventSourcedRoot aggregate) {
        EventStream eventStream = eventStore.loadEventStreamSince(
                eventStreamId.withVersion(aggregate.getVersion() + 1L));
        if (!eventStream.getEvents().isEmpty()) {
            aggregate.replay(eventStream.getEvents(), eventStream.getVersion());
        }
        return (T) aggregate;
    }

    private <T extends AggregateRoot> AbstractEventSourcedRoot restoreFromSnapshot(Class<T> aggregateType,
                                                                                   EventStreamId eventStreamId) {
        if (snapshotStore == null) {
            return null;
        }

        AbstractEventSourcedRoot aggregate = (AbstractEventSourcedRoot) snapshotStore.restore(
                eventStreamId,
                aggregateType);
        if (aggregate != null) {
            aggregate.initializeMutationMap();
        }
        return aggregate;
    }

    @Override
    public <T extends AggregateRoot> void save(AggregateId aggregateId, T aggregateRoot) {
        AbstractEventSourcedRoot eventSourcedRoot = (AbstractEventSourcedRoot) aggregateRoot;

        EventStreamId eventStreamId = new EventStreamId(aggregateId.getId());
        long version = eventSourcedRoot.getVersion();
        List<DomainEvent> pendingEvents = eventSourcedRoot.getPendingEvents();

        try {
            eventStore.appendWith(
                    eventStreamId.withVersion(version - pendingEvents.size() + 1L),
                    pendingEvents);
            eventSourcedRoot.commitEvents(pendingEvents);

            EventStreamId currentEventStreamId = eventStreamId.withVersion(version);
            if (snapshotStore != null && snapshotStore.isNeedSnapshot(currentEventStreamId)) {
                snapshotStore.snapshot(
                        currentEventStreamId,
                        aggregateRoot);
            }
        } catch (EventStoreConcurrencyException e) {
            throw new IllegalStateException("EventSourcedAggregateRoot may be not saved", e);
        }
    }
}
