package net.lulab.drived.domain.model.fixture;

import lombok.*;
import net.lulab.drived.event.DomainEvent;

import java.time.ZonedDateTime;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class AlbumReleased implements DomainEvent {

    @NonNull
    private MusicArtistId id;

    private int albumSeq;

    @NonNull
    private String albumName;

    @NonNull
    private ZonedDateTime releasedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlbumReleased that = (AlbumReleased) o;
        return albumSeq == that.albumSeq &&
                Objects.equals(id, that.id) &&
                Objects.equals(albumName, that.albumName) &&
                releasedAt.isEqual(that.releasedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, albumSeq, albumName, releasedAt);
    }
}
