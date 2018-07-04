package net.lulab.drived.persistence.event.sourcing.jpa;

import net.lulab.drived.event.sourcing.DispatchableDomainEvent;
import net.lulab.drived.event.sourcing.DomainEventSerializer;
import net.lulab.drived.event.sourcing.EventDispatcher;
import net.lulab.drived.event.sourcing.EventNotifiable;
import net.lulab.drived.persistence.event.sourcing.jackson.JacksonDomainEventSerializer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JpaFollowEventDispatcher implements EventDispatcher, EventNotifiable {

    private final EntityManagerFactory entityManagerFactory;
    private final DomainEventSerializer domainEventSerializer;

    private final List<EventDispatcher> dispatchers;

    public JpaFollowEventDispatcher(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.domainEventSerializer = new JacksonDomainEventSerializer();

        this.dispatchers = new ArrayList<>();
    }

    @Override
    public void dispatch(DispatchableDomainEvent event) {
        for (EventDispatcher dispatcher : dispatchers) {
            if (dispatcher.isAbleDispatch(event)) {
                dispatcher.dispatch(event);
            }
        }
    }

    @Override
    public void registerDispatcher(EventDispatcher dispatcher) {
        this.dispatchers.add(dispatcher);
    }

    @Override
    public boolean isAbleDispatch(DispatchableDomainEvent event) {
        return true;
    }

    @Override
    public void notifyDispatchableDomainEvents() {
        long lastDispatchedId = findLastDispatchedId();

        List<StoredEventEntity> storedEvents = loadStoredEventAfter(lastDispatchedId);
        for (StoredEventEntity storedEvent : storedEvents) {
            dispatch(new DispatchableDomainEvent(
                    storedEvent.getStreamName(),
                    storedEvent.getVersion(),
                    storedEvent.getVersionedAt(),
                    domainEventSerializer.deserialize(storedEvent.getData())));

            saveLastDispatchedId(storedEvent.getEventId());
        }
    }

    private void saveLastDispatchedId(long dispatchedId) {
        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();

            em.merge(new LastDispatchedStoredEventIdEntity(dispatchedId));

            transaction.commit();
        } finally {
            em.close();
        }
    }

    private long findLastDispatchedId() {
        EntityManager em = entityManagerFactory.createEntityManager();

        LastDispatchedStoredEventIdEntity dispatchedId = em.find(
                LastDispatchedStoredEventIdEntity.class,
                LastDispatchedStoredEventIdEntity.FIXED_ID);
        em.close();

        return Optional.ofNullable(dispatchedId)
                .map(LastDispatchedStoredEventIdEntity::getLastDispatchedEventId)
                .orElse(0L);
    }

    private List<StoredEventEntity> loadStoredEventAfter(long eventId) {
        EntityManager em = entityManagerFactory.createEntityManager();

        List<StoredEventEntity> storedEvents = em.createQuery(
                "SELECT d FROM StoredEventEntity d"
                        + " WHERE d.id > :eventId",
                StoredEventEntity.class)
                .setParameter("eventId", eventId)
                .getResultList();
        em.close();

        return storedEvents;
    }
}
