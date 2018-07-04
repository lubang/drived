package net.lulab.drived.domain.model.fixture;

import lombok.*;
import net.lulab.drived.event.DomainEvent;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
public class MusicArtistNamed implements DomainEvent {

    private String name;

}
