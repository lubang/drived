package net.lulab.drived.persistence.event.sourcing.hashmap.fixture;

import net.lulab.drived.event.DomainEvent;

import java.time.ZonedDateTime;

public class HashMapStoredEvent {

    private final long eventId;
    private final String streamName;
    private final long version;
    private final ZonedDateTime versionedAt;
    private final DomainEvent domainEvent;

    public HashMapStoredEvent(long eventId,
                              String streamName,
                              long version,
                              DomainEvent domainEvent) {

        this.eventId = eventId;
        this.streamName = streamName;
        this.version = version;
        this.versionedAt = ZonedDateTime.now();
        this.domainEvent = domainEvent;
    }

    public long getEventId() {
        return eventId;
    }

    public String getStreamName() {
        return streamName;
    }

    public long getVersion() {
        return version;
    }

    public ZonedDateTime getVersionedAt() {
        return versionedAt;
    }

    public DomainEvent getDomainEvent() {
        return domainEvent;
    }
}
