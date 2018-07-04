package net.lulab.drived.domain.model.fixture;

import lombok.*;
import net.lulab.drived.domain.model.AggregateId;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
public class MusicArtistId implements AggregateId {

    private String id;

    public static MusicArtistId createUniqueId() {
        return new MusicArtistId(UUID.randomUUID().toString().toUpperCase());
    }
}
