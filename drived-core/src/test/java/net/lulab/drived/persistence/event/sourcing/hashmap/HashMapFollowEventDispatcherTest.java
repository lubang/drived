package net.lulab.drived.persistence.event.sourcing.hashmap;

import net.lulab.drived.domain.model.fixture.MusicArtistNamed;
import net.lulab.drived.event.DomainEvent;
import net.lulab.drived.event.sourcing.DispatchableDomainEvent;
import net.lulab.drived.event.sourcing.EventDispatcher;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapDatabaseProvider;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapFollowEventDispatcher;
import net.lulab.drived.persistence.event.sourcing.hashmap.fixture.HashMapStoredEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

public class HashMapFollowEventDispatcherTest {

    private final Map<Long, HashMapStoredEvent> database = HashMapDatabaseProvider.getDatabase();

    @Before
    public void setup() {
        database.clear();
        append10Events(1L);
    }

    @After
    public void teardown() {
        database.clear();
    }

    @Test
    public void dispatch_all_events_when_event_notifier_notifies() {
        ListEventDispatcher eventDispatcher = new ListEventDispatcher();

        HashMapFollowEventDispatcher dispatcher = new HashMapFollowEventDispatcher(database);
        dispatcher.registerDispatcher(eventDispatcher);
        dispatcher.notifyDispatchableDomainEvents();

        DispatchableDomainEvent actual = eventDispatcher.getDispatchableDomainEvents().get(1);
        assertEquals(10, eventDispatcher.getDispatchableDomainEvents().size());
        assertEquals("TEST_DISPATCH", actual.getStreamName());
        assertEquals(2L, actual.getVersion());
        assertTrue(Duration.between(actual.getVersionedAt(), ZonedDateTime.now()).toMillis() < 1000);
        assertEquals(new MusicArtistNamed("a2"), actual.getDomainEvent());
    }

    @Test
    public void dispatch_all_events_twice() {
        ListEventDispatcher eventDispatcher = new ListEventDispatcher();

        HashMapFollowEventDispatcher eventNotifier = new HashMapFollowEventDispatcher(database);
        eventNotifier.registerDispatcher(eventDispatcher);
        eventNotifier.notifyDispatchableDomainEvents();
        eventDispatcher.getDispatchableDomainEvents().clear();

        append10Events(11L);

        eventNotifier.notifyDispatchableDomainEvents();

        DispatchableDomainEvent actual = eventDispatcher.getDispatchableDomainEvents().get(1);
        assertEquals(10, eventDispatcher.getDispatchableDomainEvents().size());
        assertEquals("TEST_DISPATCH", actual.getStreamName());
        assertEquals(12L, actual.getVersion());
        assertTrue(Duration.between(actual.getVersionedAt(), ZonedDateTime.now()).toMillis() < 1000);
        assertEquals(new MusicArtistNamed("a2"), actual.getDomainEvent());
    }

    @Test
    public void is_able_dispatch_returns_true_always() {
        HashMapFollowEventDispatcher eventNotifier = new HashMapFollowEventDispatcher(database);
        boolean actual = eventNotifier.isAbleDispatch(any());

        assertTrue(actual);
    }

    private void append10Events(long startVersion) {
        List<DomainEvent> events = Arrays.asList(
                new MusicArtistNamed("a1"),
                new MusicArtistNamed("a2"),
                new MusicArtistNamed("a3"),
                new MusicArtistNamed("a4"),
                new MusicArtistNamed("a5"),
                new MusicArtistNamed("a6"),
                new MusicArtistNamed("a7"),
                new MusicArtistNamed("a8"),
                new MusicArtistNamed("a9"),
                new MusicArtistNamed("a10"));

        long expectedVersion = startVersion;

        for (DomainEvent event : events) {
            database.put(
                    expectedVersion,
                    new HashMapStoredEvent(
                            expectedVersion,
                            "TEST_DISPATCH",
                            expectedVersion,
                            event));
            expectedVersion++;
        }
    }

    class ListEventDispatcher implements EventDispatcher {
        private List<DispatchableDomainEvent> dispatchableDomainEvents = new ArrayList<>();

        List<DispatchableDomainEvent> getDispatchableDomainEvents() {
            return dispatchableDomainEvents;
        }

        @Override
        public void dispatch(DispatchableDomainEvent event) {
            dispatchableDomainEvents.add(event);
        }

        @Override
        public void registerDispatcher(EventDispatcher dispatcher) {
        }

        @Override
        public boolean isAbleDispatch(DispatchableDomainEvent event) {
            return true;
        }
    }
}