package net.lulab.drived.event.sourcing;

import net.lulab.drived.domain.model.fixture.*;
import net.lulab.drived.event.DomainEvent;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventSourcedRootTest {

    @Test
    public void raise_events_and_get_pending_events_when_music_artist_created() {
        MusicArtistId id = MusicArtistId.createUniqueId();
        MusicArtist musicArtist = new MusicArtist(id,
                "Red Velvet",
                ZonedDateTime.parse("2014-08-01T00:00:00+09:00"));

        long actualVersion = musicArtist.getVersion();
        List<DomainEvent> pendingEvents = musicArtist.getPendingEvents();

        assertEquals(1L, actualVersion);
        assertEquals(1L, pendingEvents.size());
        assertEquals(new MusicArtistCreated(id,
                        "Red Velvet",
                        ZonedDateTime.parse("2014-08-01T00:00:00+09:00")),
                pendingEvents.get(0));
    }

    @Test
    public void apply_events_when_music_artist_releases_an_album() {
        MusicArtistId id = MusicArtistId.createUniqueId();
        MusicArtist musicArtist = new MusicArtist(id,
                "Red Velvet",
                ZonedDateTime.parse("2014-08-01T00:00:00+09:00"));
        musicArtist.releaseAlbum("The Red", ZonedDateTime.parse("2015-09-09T00:00:00+09:00"));

        assertEquals("Red Velvet", musicArtist.getArtistName());
        assertEquals(
                Collections.singletonList(new Album(1,
                        "The Red",
                        ZonedDateTime.parse("2015-09-09T00:00:00+09:00"))),
                musicArtist.getAlbums());
    }

    @Test
    public void commit_events_is_delete_pending_events() {
        MusicArtistId id = MusicArtistId.createUniqueId();
        MusicArtist musicArtist = new MusicArtist(id,
                "Red Velvet",
                ZonedDateTime.parse("2014-08-01T00:00:00+09:00"));
        musicArtist.releaseAlbum("The Red", ZonedDateTime.parse("2015-09-09T00:00:00+09:00"));

        List<DomainEvent> pendingEvents = musicArtist.getPendingEvents();
        musicArtist.commitEvents(pendingEvents);

        List<DomainEvent> actual = musicArtist.getPendingEvents();
        assertTrue(actual.isEmpty());
    }

    @Test
    public void replay_events_with_music_artist_releases_an_album() {
        MusicArtistId id = MusicArtistId.createUniqueId();

        EventStream eventStream = new EventStream(Arrays.asList(
                new MusicArtistCreated(id,
                        "Red Velvet",
                        ZonedDateTime.parse("2014-08-01T00:00:00+09:00")),
                new AlbumReleased(id,
                        1,
                        "The Red",
                        ZonedDateTime.parse("2015-09-09T00:00:00+09:00"))
        ), 2L);

        MusicArtist musicArtist = new MusicArtist(eventStream.getEvents(), eventStream.getVersion());
        assertEquals("Red Velvet", musicArtist.getArtistName());
        assertEquals(
                Collections.singletonList(new Album(1,
                        "The Red",
                        ZonedDateTime.parse("2015-09-09T00:00:00+09:00"))),
                musicArtist.getAlbums());
    }

}
