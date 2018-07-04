package net.lulab.drived.domain.model.fixture;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.lulab.drived.event.DomainEvent;
import net.lulab.drived.event.sourcing.AbstractEventSourcedRoot;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
public class MusicArtist extends AbstractEventSourcedRoot {

    private MusicArtistId id;

    @Getter
    private String artistName;

    @Getter
    private List<Album> albums;

    private MusicArtist() {
        super();
    }

    @Override
    protected void initializeMutationMap() {
        addMutate(MusicArtistCreated.class, this::mutate);
        addMutate(AlbumReleased.class, this::mutate);
    }

    public MusicArtist(MusicArtistId id,
                       String artistName,
                       ZonedDateTime debutedAt) {
        this();
        raiseEvent(new MusicArtistCreated(id, artistName, debutedAt));
    }

    public MusicArtist(List<DomainEvent> events, long version) {
        this();
        replay(events, version);
    }

    public void releaseAlbum(String albumName, ZonedDateTime releasedAt) {
        int albumSeq = this.albums.size() + 1;

        raiseEvent(new AlbumReleased(
                id,
                albumSeq,
                albumName,
                releasedAt));
    }

    private void mutate(MusicArtistCreated event) {
        this.id = event.getId();
        this.artistName = event.getArtistName();
        this.albums = new ArrayList<>();
    }

    private void mutate(AlbumReleased event) {
        this.albums.add(new Album(
                event.getAlbumSeq(),
                event.getAlbumName(),
                event.getReleasedAt()
        ));
    }
}
