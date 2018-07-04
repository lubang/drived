package net.lulab.drived.domain.model.fixture;

import lombok.*;
import net.lulab.drived.domain.model.AggregateId;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
public class ActArtistId implements AggregateId {

    private String id;

    public static ActArtistId createUniqueId() {
        return new ActArtistId(UUID.randomUUID().toString().toUpperCase());
    }
}
