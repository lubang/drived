package net.lulab.drived.event.sourcing;

import net.lulab.drived.event.DomainEvent;

import java.time.ZonedDateTime;
import java.util.List;

public interface EventStore {

    void appendWith(EventStreamId startingIdentity, List<DomainEvent> events)
            throws EventStoreConcurrencyException;

    EventStream loadEventStreamSince(EventStreamId streamId);

    EventStream loadEventStreamPeriod(EventStreamId streamId,
                                      ZonedDateTime startTime,
                                      ZonedDateTime endTime);

    void registerEventNotifiable(EventNotifiable eventNotifiable);

}
