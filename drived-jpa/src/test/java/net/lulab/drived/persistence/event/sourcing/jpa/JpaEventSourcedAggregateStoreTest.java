package net.lulab.drived.persistence.event.sourcing.jpa;

import net.lulab.drived.domain.model.AggregateStore;
import net.lulab.drived.event.sourcing.EventSourcedAggregateStore;
import net.lulab.drived.event.sourcing.EventSourcedAggregateStoreTestCase;
import net.lulab.drived.persistence.event.sourcing.jpa.fixture.EntityManagerProvider;
import org.junit.Before;

import javax.persistence.EntityManagerFactory;

public class JpaEventSourcedAggregateStoreTest extends EventSourcedAggregateStoreTestCase {

    private EntityManagerFactory entityManagerFactory;

    @Before
    public void setup() {
        entityManagerFactory = EntityManagerProvider.getInstance();
    }

    @Override
    protected AggregateStore getAggregateStore() {
        return new EventSourcedAggregateStore(
                new JpaEventStore(entityManagerFactory),
                new JpaSnapshotStore(entityManagerFactory));
    }

    @Override
    protected AggregateStore getAggregateStoreWitNullSnapshot() {
        return new EventSourcedAggregateStore(
                new JpaEventStore(entityManagerFactory),
                null);
    }
}