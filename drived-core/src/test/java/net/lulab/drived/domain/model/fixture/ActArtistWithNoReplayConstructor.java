package net.lulab.drived.domain.model.fixture;

import net.lulab.drived.event.sourcing.AbstractEventSourcedRoot;

public class ActArtistWithNoReplayConstructor extends AbstractEventSourcedRoot {

    public ActArtistWithNoReplayConstructor() {
    }

    @Override
    protected void initializeMutationMap() {
    }
    // No implementation invokes IllegalStateException

}
