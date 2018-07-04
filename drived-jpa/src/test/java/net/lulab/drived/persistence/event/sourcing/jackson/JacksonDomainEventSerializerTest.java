package net.lulab.drived.persistence.event.sourcing.jackson;

import net.lulab.drived.event.DomainEvent;
import net.lulab.drived.event.sourcing.DomainEventSerializer;
import net.lulab.drived.domain.model.fixture.MusicArtistCreated;
import net.lulab.drived.domain.model.fixture.MusicArtistId;
import net.lulab.drived.domain.model.fixture.MusicArtistThrowableEvent;
import org.junit.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class JacksonDomainEventSerializerTest {

    private final DomainEventSerializer serializer = new JacksonDomainEventSerializer();

    @Test
    public void serialize_and_deserialize_with_binary_format() {
        MusicArtistId id = MusicArtistId.createUniqueId();
        MusicArtistCreated event = new MusicArtistCreated(id,
                "Red Velvet",
                ZonedDateTime.parse("2014-08-01T00:00:00+09:00"));

        byte[] bytes = serializer.serialize(event);
        assertNotNull(bytes);
        DomainEvent actual = serializer.deserialize(bytes);

        assertNotNull(actual);
        assertEquals(event, actual);
    }

    @Test
    public void serialize_and_deserialize_json_format() {
        MusicArtistId id = MusicArtistId.createUniqueId();
        MusicArtistCreated event = new MusicArtistCreated(id,
                "Red Velvet",
                ZonedDateTime.parse("2014-08-01T00:00:00+09:00"));

        byte[] bytes = serializer.serialize(event);
        String json = new String(bytes);
        DomainEvent actual = serializer.deserialize(bytes);

        assertNotNull(actual);
        assertEquals(event, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void serialize_throws_exception_when_events_is_null() {
        serializer.serialize(null);

        fail("should be thrown `IllegalArgumentException`");
    }

    @Test(expected = IllegalArgumentException.class)
    public void serialize_throws_exception_when_domain_event_type_is_anonymous() {
        serializer.serialize((new DomainEvent() {
            @Override
            public String toString() {
                return "Hello Test";
            }
        }));

        fail("should be thrown `IllegalArgumentException`");
    }

    @Test(expected = IllegalArgumentException.class)
    public void serialize_throws_exception_when_domain_event_throws_exception() {
        serializer.serialize(new MusicArtistThrowableEvent());

        fail("should be thrown `IllegalArgumentException`");
    }

    @Test(expected = IllegalArgumentException.class)
    public void deserialize_throws_exception_when_json_is_invalid() {
        final byte[] data = "[{\"even.ddd.event.sourcing,\"occurredAt\":1539907200.000000000}]".getBytes();

        serializer.deserialize(data);

        fail("should be thrown `IllegalArgumentException`");

    }
}