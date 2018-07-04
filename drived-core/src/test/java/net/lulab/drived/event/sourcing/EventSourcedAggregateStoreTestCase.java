package net.lulab.drived.event.sourcing;

import net.lulab.drived.domain.model.AggregateStore;
import net.lulab.drived.domain.model.fixture.*;
import org.junit.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public abstract class EventSourcedAggregateStoreTestCase {

    protected abstract AggregateStore getAggregateStore();

    protected abstract AggregateStore getAggregateStoreWitNullSnapshot();

    @Test
    public void save_and_load_an_aggregate_with_jpa_snapshot_and_jpa_event_store() {
        AggregateStore aggregateStore = getAggregateStore();

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

    @Test(expected = IllegalStateException.class)
    public void save_an_aggregate_throws_exception_when_event_store_throws_event_store_concurrency_exception()
            throws EventStoreConcurrencyException {

        EventStore eventStore = mock(EventStore.class);
        doThrow(EventStoreConcurrencyException.class)
                .when(eventStore)
                .appendWith(any(EventStreamId.class), anyList());
        AggregateStore aggregateStore = new EventSourcedAggregateStore(
                eventStore,
                null);

        MusicArtistId id = MusicArtistId.createUniqueId();
        MusicArtist musicArtist = new MusicArtist(
                id,
                "Red Velvet",
                ZonedDateTime.parse("2014-08-01T00:00:00+09:00"));
        aggregateStore.save(id, musicArtist);

        fail("It's failed because EventSourcedAggregateStore catch an EventStoreConcurrencyException"
                + " and it throws IllegalStateException");
    }

    @Test(expected = IllegalStateException.class)
    public void load_an_aggregate_throws_exception_when_aggregate_has_not_a_constructor_for_replaying() {
        AggregateStore aggregateStore = getAggregateStoreWitNullSnapshot();
        ActArtistId actArtistId = ActArtistId.createUniqueId();
        ActArtistWithNoReplayConstructor actArtist = new ActArtistWithNoReplayConstructor();

        aggregateStore.save(actArtistId, actArtist);
        aggregateStore.load(actArtistId, ActArtistWithNoReplayConstructor.class);

        fail("It's failed because ActArtistWithNoReplayConstructor has not a constructor for replaying");
    }

    @Test(expected = IllegalStateException.class)
    public void load_an_aggregate_throws_exception_when_aggregate_constructor_throws_while_replaying() {
        AggregateStore aggregateStore = getAggregateStoreWitNullSnapshot();
        ActArtistId actArtistId = ActArtistId.createUniqueId();
        ActArtistWithThrowReplayConstructor actArtist = new ActArtistWithThrowReplayConstructor();

        aggregateStore.save(actArtistId, actArtist);
        aggregateStore.load(actArtistId, ActArtistWithThrowReplayConstructor.class);

        fail("It's failed because ActArtistWithThrowReplayConstructor throws while replaying");
    }
}