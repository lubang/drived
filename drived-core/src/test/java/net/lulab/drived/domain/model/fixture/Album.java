package net.lulab.drived.domain.model.fixture;

import lombok.*;
import net.lulab.drived.domain.model.ValueObject;

import java.time.ZonedDateTime;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Album implements ValueObject {

    private int albumSeq;

    @NonNull
    private String albumName;

    @NonNull
    private ZonedDateTime releasedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return albumSeq == album.albumSeq &&
                Objects.equals(albumName, album.albumName) &&
                releasedAt.isEqual(album.releasedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(albumSeq, albumName, releasedAt);
    }
}
