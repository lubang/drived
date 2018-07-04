package net.lulab.drived.event.sourcing;

import net.lulab.drived.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;

public class EventStream {

    private final List<DomainEvent> events;

    private final long version;

    public EventStream() {
        this(new ArrayList<>(), 0L);
    }

    public EventStream(List<DomainEvent> events, long version) {
        this.events = events;
        this.version = version;
    }

    public List<DomainEvent> getEvents() {
        return events;
    }

    public long getVersion() {
        return version;
    }
}
