package net.lulab.drived.event.sourcing;

import net.lulab.drived.event.DomainEvent;

import java.time.ZonedDateTime;

public class DispatchableDomainEvent implements DomainEvent {

    private final String streamName;

    private final long version;

    private final ZonedDateTime versionedAt;

    private final DomainEvent domainEvent;

    public DispatchableDomainEvent(String streamName,
                                   long version,
                                   ZonedDateTime versionedAt,
                                   DomainEvent domainEvent) {
        this.streamName = streamName;
        this.version = version;
        this.versionedAt = versionedAt;
        this.domainEvent = domainEvent;
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
