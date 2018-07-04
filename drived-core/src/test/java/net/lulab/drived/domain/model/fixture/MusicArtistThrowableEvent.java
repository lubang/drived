package net.lulab.drived.domain.model.fixture;

import net.lulab.drived.event.DomainEvent;

public final class MusicArtistThrowableEvent implements DomainEvent {

    public String getName() {
        throw new IllegalStateException();
    }

}
