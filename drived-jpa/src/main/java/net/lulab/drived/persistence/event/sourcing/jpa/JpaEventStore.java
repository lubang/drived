package net.lulab.drived.persistence.event.sourcing.jpa;

import net.lulab.drived.event.DomainEvent;
import net.lulab.drived.event.sourcing.*;
import net.lulab.drived.persistence.event.sourcing.jackson.JacksonDomainEventSerializer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpaEventStore implements EventStore {

    private final EntityManagerFactory entityManagerFactory;
    private final DomainEventSerializer domainEventSerializer;

    private EventNotifiable eventNotifiable;

    public JpaEventStore(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.domainEventSerializer = new JacksonDomainEventSerializer();
    }

    @Override
    public void appendWith(EventStreamId startingIdentity,
                           List<DomainEvent> events)
            throws EventStoreConcurrencyException {

        EntityManager em = entityManagerFactory.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            long expectedVersion = startingIdentity.getVersion();
            long actualVersion = getNextVersion(startingIdentity.getStreamName());
            if (expectedVersion != actualVersion) {
                throw new EventStoreConcurrencyException(expectedVersion,
                        actualVersion,
                        startingIdentity.getStreamName());
            }

            for (DomainEvent event : events) {
                em.persist(
                        new StoredEventEntity(
                                startingIdentity.getStreamName(),
                                expectedVersion,
                                domainEventSerializer.serialize(event)));
                expectedVersion++;
            }

            tx.commit();

            if (eventNotifiable != null) {
                eventNotifiable.notifyDispatchableDomainEvents();
            }

        } catch (EventStoreConcurrencyException | Exception e) {
            tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public EventStream loadEventStreamSince(EventStreamId streamId) {
        EntityManager em = entityManagerFactory.createEntityManager();

        List<StoredEventEntity> results = em.createQuery(
                "SELECT d FROM StoredEventEntity d"
                        + " WHERE d.streamName = :streamName"
                        + " AND d.version >= :version",
                StoredEventEntity.class)
                .setParameter("streamName", streamId.getStreamName())
                .setParameter("version", streamId.getVersion())
                .getResultList();

        return convertEventStream(results);
    }

    @Override
    public EventStream loadEventStreamPeriod(EventStreamId streamId,
                                             ZonedDateTime startTime,
                                             ZonedDateTime endTime) {
        EntityManager em = entityManagerFactory.createEntityManager();

        List<StoredEventEntity> results = em.createQuery(
                "SELECT d FROM StoredEventEntity d"
                        + " WHERE d.streamName = :streamName"
                        + " AND d.version >= :version"
                        + " AND d.versionedAt >= :startTime"
                        + " AND d.versionedAt < :endTime",
                StoredEventEntity.class)
                .setParameter("streamName", streamId.getStreamName())
                .setParameter("version", streamId.getVersion())
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .getResultList();

        return convertEventStream(results);
    }

    @Override
    public void registerEventNotifiable(EventNotifiable eventNotifiable) {
        this.eventNotifiable = eventNotifiable;
    }

    private long getNextVersion(String streamName) {
        EntityManager em = entityManagerFactory.createEntityManager();

        final Long version = em.createQuery(
                "SELECT MAX(s.version)"
                        + " FROM StoredEventEntity s"
                        + " WHERE s.streamName = :streamName",
                Long.class)
                .setParameter("streamName", streamName)
                .getSingleResult();
        return Optional.ofNullable(version).orElse(0L) + 1L;
    }

    private EventStream convertEventStream(List<StoredEventEntity> results) {
        if (results == null || results.isEmpty()) {
            return new EventStream(new ArrayList<>(), 0L);
        }

        List<DomainEvent> events = results.stream()
                .map(r -> domainEventSerializer.deserialize(r.getData()))
                .collect(Collectors.toList());

        return new EventStream(events, results.get(results.size() - 1).getVersion());
    }
}
