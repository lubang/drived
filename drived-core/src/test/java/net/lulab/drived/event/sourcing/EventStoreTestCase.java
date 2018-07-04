package net.lulab.drived.event.sourcing;

import net.lulab.drived.domain.model.fixture.MusicArtistNamed;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public abstract class EventStoreTestCase {

    private EventStore eventStore;

    protected abstract EventStore getEventStore();

    protected abstract EventStreamId getEventStreamId();

    @Before
    public void setup() {
        this.eventStore = getEventStore();
    }

    @After
    public void teardown() {
        this.eventStore = null;
    }

    @Test
    public void load_event_stream_when_event_store_is_empty() {
        EventStream emptyActual = eventStore.loadEventStreamSince(
                getEventStreamId());

        assertEquals(0L, emptyActual.getVersion());
        assertTrue(emptyActual.getEvents().isEmpty());
    }

    @Test
    public void load_event_stream_by_skip_version_when_event_store_is_empty() {
        EventStream emptySkippedActual = eventStore.loadEventStreamSince(
                getEventStreamId().withVersion(2L));

        assertEquals(0L, emptySkippedActual.getVersion());
        assertTrue(emptySkippedActual.getEvents().isEmpty());
    }

    @Test
    public void load_event_stream_by_event_versioned_at_when_streams_is_not_exist() {
        final ZonedDateTime startAt = ZonedDateTime.now().minusYears(1);
        final ZonedDateTime endAt = ZonedDateTime.now();

        EventStream actual = eventStore.loadEventStreamPeriod(
                getEventStreamId(),
                startAt,
                endAt);

        assertNotNull(actual);
        assertEquals(0L, actual.getVersion());
        assertEquals(0, actual.getEvents().size());
    }

    @Test
    public void append_events_and_load_event_stream()
            throws EventStoreConcurrencyException {

        EventStreamId eventStreamId = getEventStreamId();

        eventStore.appendWith(
                eventStreamId,
                Arrays.asList(
                        new MusicArtistNamed("a"),
                        new MusicArtistNamed("b")));

        EventStream allActual = eventStore.loadEventStreamSince(eventStreamId);
        assertNotNull(allActual);
        assertEquals(2L, allActual.getVersion());
        assertEquals(2, allActual.getEvents().size());
        assertEquals(new MusicArtistNamed("a"), allActual.getEvents().get(0));
        assertEquals(new MusicArtistNamed("b"), allActual.getEvents().get(1));
    }

    @Test
    public void append_events_and_load_event_stream_by_event_stream_name()
            throws EventStoreConcurrencyException {

        EventStreamId eventStreamId = getEventStreamId();

        eventStore.appendWith(
                eventStreamId,
                Arrays.asList(
                        new MusicArtistNamed("a"),
                        new MusicArtistNamed("b")));
        String anotherStreamName = "Another stream" + UUID.randomUUID().toString();
        eventStore.appendWith(
                new EventStreamId(anotherStreamName),
                Arrays.asList(
                        new MusicArtistNamed("another-c"),
                        new MusicArtistNamed("another-d")));

        EventStream allActual = eventStore.loadEventStreamSince(eventStreamId);
        assertNotNull(allActual);
        assertEquals(2L, allActual.getVersion());
        assertEquals(2, allActual.getEvents().size());
        assertEquals(new MusicArtistNamed("a"), allActual.getEvents().get(0));
        assertEquals(new MusicArtistNamed("b"), allActual.getEvents().get(1));


        EventStream anotherActual = eventStore.loadEventStreamSince(
                new EventStreamId(anotherStreamName));
        assertNotNull(anotherActual);
        assertEquals(2L, anotherActual.getVersion());
        assertEquals(2, anotherActual.getEvents().size());
        assertEquals(new MusicArtistNamed("another-c"), anotherActual.getEvents().get(0));
        assertEquals(new MusicArtistNamed("another-d"), anotherActual.getEvents().get(1));
    }

    @Test
    public void append_events_and_load_event_stream_by_version()
            throws EventStoreConcurrencyException {

        EventStreamId eventStreamId = getEventStreamId();

        eventStore.appendWith(
                eventStreamId,
                Arrays.asList(
                        new MusicArtistNamed("c1"),
                        new MusicArtistNamed("c2"),
                        new MusicArtistNamed("c3"),
                        new MusicArtistNamed("c4"),
                        new MusicArtistNamed("c5"),
                        new MusicArtistNamed("c6"),
                        new MusicArtistNamed("c7"),
                        new MusicArtistNamed("c8"),
                        new MusicArtistNamed("c9"),
                        new MusicArtistNamed("c10")));

        EventStream actual = eventStore.loadEventStreamSince(eventStreamId.withVersion(7));

        assertNotNull(actual);
        assertEquals(10L, actual.getVersion());
        assertEquals(4, actual.getEvents().size());
        assertEquals(new MusicArtistNamed("c7"), actual.getEvents().get(0));
        assertEquals(new MusicArtistNamed("c8"), actual.getEvents().get(1));
        assertEquals(new MusicArtistNamed("c9"), actual.getEvents().get(2));
        assertEquals(new MusicArtistNamed("c10"), actual.getEvents().get(3));
    }

    @Test
    public void append_events_and_load_event_stream_by_event_versioned_at()
            throws InterruptedException, EventStoreConcurrencyException {

        String anotherStreamName = "Another stream" + UUID.randomUUID().toString();
        eventStore.appendWith(
                new EventStreamId(anotherStreamName),
                Arrays.asList(
                        new MusicArtistNamed("another-c"),
                        new MusicArtistNamed("another-d")));

        EventStreamId eventStreamId = getEventStreamId();

        eventStore.appendWith(
                eventStreamId,
                Arrays.asList(
                        new MusicArtistNamed("a"),
                        new MusicArtistNamed("b")));

        waitForChangingZonedDateTime();
        ZonedDateTime startedAt = ZonedDateTime.now();
        waitForChangingZonedDateTime();

        eventStore.appendWith(
                eventStreamId.withVersion(3L),
                Arrays.asList(
                        new MusicArtistNamed("c"),
                        new MusicArtistNamed("d")));
        eventStore.appendWith(
                eventStreamId.withVersion(5L),
                Arrays.asList(
                        new MusicArtistNamed("e"),
                        new MusicArtistNamed("f"),
                        new MusicArtistNamed("g")));

        waitForChangingZonedDateTime();
        ZonedDateTime finishedAt = ZonedDateTime.now();
        waitForChangingZonedDateTime();

        eventStore.appendWith(
                eventStreamId.withVersion(8L),
                Arrays.asList(
                        new MusicArtistNamed("h"),
                        new MusicArtistNamed("i")));

        EventStream actual = eventStore.loadEventStreamPeriod(
                eventStreamId,
                startedAt,
                finishedAt);

        assertNotNull(actual);
        assertEquals(7L, actual.getVersion());
        assertEquals(5, actual.getEvents().size());
    }

    private void waitForChangingZonedDateTime() throws InterruptedException {
        Thread.sleep(100);
    }

    @Test
    public void append_events_with_event_stream_next_version() throws EventStoreConcurrencyException {
        EventStreamId eventStreamId = getEventStreamId();

        eventStore.appendWith(
                eventStreamId,
                Collections.singletonList(
                        new MusicArtistNamed("a")));
        eventStore.appendWith(
                eventStreamId.withVersion(2L),
                Collections.singletonList(
                        new MusicArtistNamed("j")));

        EventStream actual = eventStore.loadEventStreamSince(eventStreamId);

        assertEquals(2L, actual.getVersion());
        assertEquals(2, actual.getEvents().size());
    }

    @Test(expected = EventStoreConcurrencyException.class)
    public void append_events_with_exist_event_version_throws_exception()
            throws EventStoreConcurrencyException {

        EventStreamId eventStreamId = getEventStreamId();

        eventStore.appendWith(
                eventStreamId,
                Collections.singletonList(
                        new MusicArtistNamed("a")));
        eventStore.appendWith(
                eventStreamId,
                Collections.singletonList(
                        new MusicArtistNamed("j")));

        fail("should be thrown `EventStoreConcurrencyException`");
    }

    @SuppressWarnings("unchecked")
    @Test(expected = Exception.class)
    public void append_events_is_rollback_when_throws_any_exception()
            throws EventStoreConcurrencyException {

        List events = mock(List.class);
        when(events).thenThrow(IllegalStateException.class);

        eventStore.appendWith(getEventStreamId(), events);

        fail("should be thrown `EventStoreConcurrencyException`");
    }

    @Test
    public void notify_events_when_event_notifiable_is_registered()
            throws EventStoreConcurrencyException {

        EventNotifiable eventNotifiable = mock(EventNotifiable.class);
        eventStore.registerEventNotifiable(eventNotifiable);

        eventStore.appendWith(
                getEventStreamId(),
                Collections.singletonList(new MusicArtistNamed("a")));

        verify(eventNotifiable, times(1)).notifyDispatchableDomainEvents();
    }
}
