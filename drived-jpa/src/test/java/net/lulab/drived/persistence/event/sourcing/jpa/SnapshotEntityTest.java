package net.lulab.drived.persistence.event.sourcing.jpa;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SnapshotEntityTest {

    @Test
    public void create_and_get_properties() {
        SnapshotEntity actual = new SnapshotEntity(
                new SnapshotEntityId("Test Stream ID", 1L),
                new byte[]{0, 1, 2});

        assertEquals("Test Stream ID", actual.getId().getStreamName());
        assertEquals(1L, actual.getId().getVersion());
        assertArrayEquals(new byte[]{0, 1, 2}, actual.getData());
    }

}