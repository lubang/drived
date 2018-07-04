package net.lulab.drived.domain.model;

public interface AggregateStore {

    <T extends AggregateRoot> T load(AggregateId id, Class<T> aggregateType);

    <T extends AggregateRoot> void save(AggregateId aggregateId, T id);

}
