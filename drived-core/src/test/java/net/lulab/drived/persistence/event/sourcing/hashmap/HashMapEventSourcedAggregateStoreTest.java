package net.lulab.drived.persistence.event.sourcing.hashmap;

import net.lulab.drived.domain.model.AggregateStore;
import net.lulab.drived.domain.model.fixture.MusicArtist;
import net.lulab.drived.domain.model.fixture.MusicArtistId;
import net.lulab.drived.event.sourcing.EventSourcedAggregateStore;
import net.lulab.drived.event.sourcing.EventSourcedAggregateStoreTestCase;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapDatabaseProvider;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapEventStore;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapSnapshotStore;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapStoredEvent;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HashMapEventSourcedAggregateStoreTest extends EventSourcedAggregateStoreTestCase {

    private Map<Long, HashMapStoredEvent> database;

    @Before
    public void setup() {
        database = HashMapDatabaseProvider.getDatabase();
        database.clear();
    }

    @Override
    protected AggregateStore getAggregateStore() {
        return new EventSourcedAggregateStore(
                new HashMapEventStore(database),
                new HashMapSnapshotStore());
    }

    @Override
    protected AggregateStore getAggregateStoreWitNullSnapshot() {
        return new EventSourcedAggregateStore(
                new HashMapEventStore(database),
                null);
    }

    @Test
    public void save_and_load_an_aggregate_without_snapshot() {
        AggregateStore aggregateStore = new EventSourcedAggregateStore(
                new HashMapEventStore(database),
                null);

        MusicArtistId id = MusicArtistId.createUniqueId();
        MusicArtist musicArtist = new MusicArtist(
                id,
                "Red Velvet",
                ZonedDateTime.parse("2014-08-01T00:00:00+09:00"));
        aggregateStore.save(id, musicArtist);

        musicArtist.releaseAlbum("Album_1", ZonedDateTime.now());
        aggregateStore.save(id, musicArtist);

        MusicArtist actual = aggregateStore.load(id, MusicArtist.class);

        assertEquals(musicArtist, actual);
    }
}
