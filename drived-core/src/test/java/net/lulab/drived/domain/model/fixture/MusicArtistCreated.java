package net.lulab.drived.domain.model.fixture;

import lombok.*;
import net.lulab.drived.event.DomainEvent;

import java.time.ZonedDateTime;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MusicArtistCreated implements DomainEvent {

    private MusicArtistId id;

    private String artistName;

    @NonNull
    private ZonedDateTime debutedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicArtistCreated that = (MusicArtistCreated) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(artistName, that.artistName) &&
                debutedAt.isEqual(that.debutedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, artistName, debutedAt);
    }
}
