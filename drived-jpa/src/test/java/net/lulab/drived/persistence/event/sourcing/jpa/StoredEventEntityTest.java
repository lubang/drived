package net.lulab.drived.persistence.event.sourcing.jpa;

import org.junit.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class StoredEventEntityTest {

    @Test
    public void create_and_get_properties() {
        String streamName = "hello";
        long version = 1L;
        byte[] data = "hello data".getBytes();

        ZonedDateTime startedAt = ZonedDateTime.now();

        StoredEventEntity actual = new StoredEventEntity(
                streamName,
                version,
                data);

        ZonedDateTime finishedAt = ZonedDateTime.now();

        assertEquals(0L, actual.getEventId());
        assertEquals("hello", actual.getStreamName());
        assertEquals(1L, actual.getVersion());
        assertArrayEquals("hello data".getBytes(), actual.getData());
        assertTrue(startedAt.isBefore(actual.getVersionedAt()) || startedAt.isEqual(actual.getVersionedAt()));
        assertTrue(finishedAt.isAfter(actual.getVersionedAt()) || finishedAt.isEqual(actual.getVersionedAt()));
    }

}