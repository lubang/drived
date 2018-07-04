package net.lulab.drived.persistence.event.sourcing.jpa;

import net.lulab.drived.domain.model.AggregateRoot;
import net.lulab.drived.event.sourcing.EventStreamId;
import net.lulab.drived.event.sourcing.SnapshotStore;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JpaSnapshotStore implements SnapshotStore {

    private final EntityManagerFactory entityManagerFactory;

    public JpaSnapshotStore(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public <T extends AggregateRoot> void snapshot(EventStreamId eventStreamId,
                                                   T aggregate) {

        EntityManager em = entityManagerFactory.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            SnapshotEntityId key = new SnapshotEntityId(
                    eventStreamId.getStreamName(),
                    eventStreamId.getVersion());
            SnapshotEntity snapshotEntity = new SnapshotEntity(
                    key,
                    serialize(aggregate));
            em.persist(snapshotEntity);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new IllegalStateException("Fail to snapshot", e);
        } finally {
            em.close();
        }
    }

    @Override
    public <T extends AggregateRoot> T restore(EventStreamId eventStreamId,
                                               Class<T> aggregateClass) {

        EntityManager em = entityManagerFactory.createEntityManager();

        try {
            SnapshotEntity snapshotEntity = em.createQuery(
                    "SELECT d FROM SnapshotEntity d"
                            + " WHERE d.id.streamName = :streamName"
                            + " AND d.id.version <= :version"
                            + " ORDER BY d.id.version DESC",
                    SnapshotEntity.class)
                    .setParameter("streamName", eventStreamId.getStreamName())
                    .setParameter("version", eventStreamId.getVersion())
                    .setMaxResults(1)
                    .getSingleResult();
            return deserialize(snapshotEntity.getData());
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean isNeedSnapshot(EventStreamId eventStreamId) {
        EntityManager em = entityManagerFactory.createEntityManager();

        String eventStreamName = eventStreamId.getStreamName();
        long startVersion = eventStreamId.getVersion() % 1000L + 1L;
        long endVersion = startVersion + 999L;

        try {
            em.createQuery(
                    "SELECT d FROM SnapshotEntity d"
                            + " WHERE d.id.streamName = :streamName"
                            + " AND d.id.version >= :startVersion"
                            + " AND d.id.version < :endVersion",
                    SnapshotEntity.class)
                    .setParameter("streamName", eventStreamName)
                    .setParameter("startVersion", startVersion)
                    .setParameter("endVersion", endVersion)
                    .setMaxResults(1)
                    .getSingleResult();
            return false;
        } catch (NoResultException e) {
            return true;
        } finally {
            em.close();
        }
    }

    private <T extends AggregateRoot> byte[] serialize(T aggregate) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(aggregate);
            oos.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new IllegalArgumentException("Fail to serialize bytes from an aggregate", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends AggregateRoot> T deserialize(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Fail to deserialize an aggregate from bytes", e);
        }
    }
}
