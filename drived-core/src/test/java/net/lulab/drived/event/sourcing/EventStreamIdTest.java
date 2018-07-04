package net.lulab.drived.event.sourcing;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EventStreamIdTest {

    @Test
    public void event_stream_id_has_stream_name_and_version() {
        EventStreamId eventStreamId = new EventStreamId("Hello Stream");

        assertEquals("Hello Stream", eventStreamId.getStreamName());
        assertEquals(1L, eventStreamId.getVersion());
    }

    @Test
    public void event_stream_id_with_version() {
        EventStreamId eventStreamId = new EventStreamId("Hello Stream");

        EventStreamId actual = eventStreamId.withVersion(3L);

        assertEquals("Hello Stream", actual.getStreamName());
        assertEquals(3L, actual.getVersion());
    }

    @Test
    public void event_stream_equals_and_hash() {
        EventStreamId eventStreamIdA = new EventStreamId("Hello Stream");
        EventStreamId eventStreamIdB = new EventStreamId("Hello Stream", 1L);

        assertEquals(eventStreamIdA, eventStreamIdB);
        assertEquals(eventStreamIdA.hashCode(), eventStreamIdB.hashCode());
    }

}