package net.lulab.drived.persistence.event.sourcing.hashmap.fixture;

import net.lulab.drived.event.DomainEvent;
import net.lulab.drived.event.sourcing.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.Collectors;

public class HashMapEventStore implements EventStore {

    private final Map<Long, HashMapStoredEvent> database;

    private EventNotifiable eventNotifiable;

    public HashMapEventStore(Map<Long, HashMapStoredEvent> database) {
        this.database = database;
    }

    @Override
    public void appendWith(EventStreamId startingIdentity, List<DomainEvent> events)
            throws EventStoreConcurrencyException {

        long expectedVersion = startingIdentity.getVersion();
        long actualVersion = getNextVersion(startingIdentity.getStreamName());

        if (expectedVersion != actualVersion) {
            throw new EventStoreConcurrencyException(expectedVersion,
                    actualVersion,
                    startingIdentity.getStreamName());
        }

        for (DomainEvent event : events) {
            HashMapStoredEvent storedEvent = new HashMapStoredEvent(
                    getNextEventId(),
                    startingIdentity.getStreamName(),
                    expectedVersion,
                    event);
            database.put(storedEvent.getEventId(), storedEvent);
            expectedVersion++;
        }

        if (eventNotifiable != null) {
            eventNotifiable.notifyDispatchableDomainEvents();
        }
    }

    @Override
    public EventStream loadEventStreamSince(EventStreamId streamId) {

        List<HashMapStoredEvent> storedEvents = database.values().stream()
                .filter(r -> isValidNameWithSince(streamId, r))
                .collect(Collectors.toList());

        if (storedEvents.isEmpty()) {
            return new EventStream();
        }

        long version = storedEvents.get(storedEvents.size() - 1).getVersion();
        List<DomainEvent> events = storedEvents.stream()
                .map(HashMapStoredEvent::getDomainEvent)
                .collect(Collectors.toList());

        return new EventStream(events, version);
    }

    @Override
    public EventStream loadEventStreamPeriod(EventStreamId streamId,
                                             ZonedDateTime startTime,
                                             ZonedDateTime endTime) {
        List<HashMapStoredEvent> storedEvents = database.values().stream()
                .filter(r -> isValidPeriod(streamId, startTime, endTime, r))
                .collect(Collectors.toList());

        if (storedEvents.isEmpty()) {
            return new EventStream();
        }

        long version = storedEvents.get(storedEvents.size() - 1).getVersion();
        List<DomainEvent> events = storedEvents.stream()
                .map(HashMapStoredEvent::getDomainEvent)
                .collect(Collectors.toList());

        return new EventStream(events, version);
    }

    @Override
    public void registerEventNotifiable(EventNotifiable eventNotifiable) {
        this.eventNotifiable = eventNotifiable;
    }

    private long getNextVersion(String streamName) {
        OptionalLong version = database.values().stream()
                .filter(e -> e.getStreamName().equals(streamName))
                .mapToLong(HashMapStoredEvent::getVersion)
                .max();
        return version.orElse(0L) + 1L;
    }

    private long getNextEventId() {
        return database.size() + 1L;
    }

    private boolean isValidPeriod(EventStreamId streamId,
                                  ZonedDateTime startTime,
                                  ZonedDateTime endTime,
                                  HashMapStoredEvent storedEvent) {

        if (isValidNameWithSince(streamId, storedEvent)) {
            ZonedDateTime versionedAt = storedEvent.getVersionedAt();
            if (versionedAt.isEqual(startTime) || versionedAt.isEqual(endTime)) {
                return true;
            }
            return versionedAt.isAfter(startTime) && versionedAt.isBefore(endTime);
        }
        return false;
    }

    private boolean isValidNameWithSince(EventStreamId streamId,
                                         HashMapStoredEvent storedEvent) {
        return streamId.getStreamName().equals(storedEvent.getStreamName())
                && streamId.getVersion() <= storedEvent.getVersion();
    }
}