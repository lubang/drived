package net.lulab.drived.persistence.event.sourcing.jpa;

import net.lulab.drived.event.sourcing.EventStore;
import net.lulab.drived.event.sourcing.EventStreamId;
import net.lulab.drived.event.sourcing.EventStoreTestCase;
import net.lulab.drived.persistence.event.sourcing.jpa.fixture.EntityManagerProvider;

import java.util.UUID;

public class JpaEventStoreTest extends EventStoreTestCase {

    @Override
    protected EventStore getEventStore() {
        return new JpaEventStore(EntityManagerProvider.getInstance());
    }

    @Override
    protected EventStreamId getEventStreamId() {
        return new EventStreamId(UUID.randomUUID().toString().toUpperCase());
    }

}