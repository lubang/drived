package net.lulab.drived.persistence.event.sourcing.hashmap;

import net.lulab.drived.event.sourcing.EventNotifiable;
import net.lulab.drived.event.sourcing.EventStore;
import net.lulab.drived.event.sourcing.EventStreamId;
import net.lulab.drived.event.sourcing.EventStoreTestCase;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapDatabaseProvider;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapEventStore;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapFollowEventDispatcher;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapStoredEvent;

import java.util.Map;
import java.util.UUID;

public class HashMapEventStoreTest extends EventStoreTestCase {

    @Override
    protected EventStore getEventStore() {
        Map<Long, HashMapStoredEvent> database = HashMapDatabaseProvider.getDatabase();

        EventNotifiable followEventDispatcher = new HashMapFollowEventDispatcher(database);
        EventStore eventStore = new HashMapEventStore(database);
        eventStore.registerEventNotifiable(followEventDispatcher);
        return eventStore;
    }

    @Override
    protected EventStreamId getEventStreamId() {
        return new EventStreamId(UUID.randomUUID().toString().toUpperCase());
    }

}
