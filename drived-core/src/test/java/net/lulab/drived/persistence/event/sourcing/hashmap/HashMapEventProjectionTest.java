package net.lulab.drived.persistence.event.sourcing.hashmap;

import net.lulab.drived.domain.model.fixture.*;
import net.lulab.drived.event.sourcing.EventDispatcher;
import net.lulab.drived.event.sourcing.EventStore;
import net.lulab.drived.event.sourcing.EventStoreConcurrencyException;
import net.lulab.drived.event.sourcing.EventStreamId;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapDatabaseProvider;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapEventStore;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapFollowEventDispatcher;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapStoredEvent;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class HashMapEventProjectionTest {
    @Test
    public void event_history_projection_invokes_when_music_artist_raise_an_event()
            throws EventStoreConcurrencyException {

        // Arrange
        MusicArtistId artistId = MusicArtistId.createUniqueId();
        Map<Long, HashMapStoredEvent> database = HashMapDatabaseProvider.getDatabase();

        HashMapFollowEventDispatcher eventDispatcher = new HashMapFollowEventDispatcher(database);
        EventHistoryRepository eventHistoryRepository = mock(EventHistoryRepository.class);
        eventDispatcher.registerDispatcher(new EventHistoryProjection(eventHistoryRepository));

        EventStore eventStore = new HashMapEventStore(database);
        eventStore.registerEventNotifiable(eventDispatcher);

        // Act
        MusicArtist redVelvet = new MusicArtist(
                artistId,
                "Red Velvet",
                ZonedDateTime.parse("2014-08-01T00:00:00+09:00"));
        eventStore.appendWith(
                new EventStreamId(artistId.getId()),
                redVelvet.getPendingEvents());

        // Assert
        verify(eventHistoryRepository, times(1))
                .add(eq(new EventHistory(
                        "`Red Velvet` artist is debuted",
                        ZonedDateTime.parse("2014-08-01T00:00:00+09:00")))
                );
    }

    @Test(expected = UnsupportedOperationException.class)
    public void event_project_does_not_register_dispatcher() {
        EventHistoryRepository eventHistoryRepository = mock(EventHistoryRepository.class);
        EventHistoryProjection eventHistoryProjection = new EventHistoryProjection(eventHistoryRepository);
        eventHistoryProjection.registerDispatcher(mock(EventDispatcher.class));

        fail("EventHistoryProjection can not register an event dispatcher");
    }
}