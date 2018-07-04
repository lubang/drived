package net.lulab.drived.domain.model.fixture;

import net.lulab.drived.event.DomainEvent;
import net.lulab.drived.event.sourcing.AbstractEventSourcedRoot;

import java.util.List;

public class ActArtistWithThrowReplayConstructor extends AbstractEventSourcedRoot {

    public ActArtistWithThrowReplayConstructor() {
        super();
    }

    public ActArtistWithThrowReplayConstructor(List<DomainEvent> events, long version) {
        this();
        this.replay(events, version);
        throw new IllegalArgumentException();
    }

    @Override
    protected void initializeMutationMap() {
    }

}

