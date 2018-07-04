package net.lulab.drived.event.sourcing;

import net.lulab.drived.domain.model.AggregateRoot;

public interface SnapshotStore {

    <T extends AggregateRoot> void snapshot(EventStreamId eventStreamId,
                                            T aggregate);

    <T extends AggregateRoot> T restore(EventStreamId eventStreamId,
                                        Class<T> aggregateClass);

    boolean isNeedSnapshot(EventStreamId version);
}
