package net.lulab.drived.event.sourcing;

import net.lulab.drived.domain.model.fixture.MusicArtistNamed;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventStreamTest {

    @Test
    public void default_event_stream_has_version_0_and_empty_events() {
        EventStream actual = new EventStream();

        assertEquals(0L, actual.getVersion());
        assertTrue(actual.getEvents().isEmpty());
    }

    @Test
    public void event_stream_carries_version_and_events() {
        EventStream eventStream = new EventStream(
                Arrays.asList(
                        new MusicArtistNamed("Red Velvet"),
                        new MusicArtistNamed("Mamamooo")),
                4L);

        assertEquals(4L, eventStream.getVersion());
        assertArrayEquals(
                Arrays.asList(
                        new MusicArtistNamed("Red Velvet"),
                        new MusicArtistNamed("Mamamooo"))
                        .toArray(),
                eventStream.getEvents().toArray());
    }
}