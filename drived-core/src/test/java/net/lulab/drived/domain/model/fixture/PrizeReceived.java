package net.lulab.drived.domain.model.fixture;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.lulab.drived.event.DomainEvent;

import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class PrizeReceived implements DomainEvent {

    private MusicArtistId id;

    private String prizeName;

    private String description;

    private ZonedDateTime receivedAt;

}
