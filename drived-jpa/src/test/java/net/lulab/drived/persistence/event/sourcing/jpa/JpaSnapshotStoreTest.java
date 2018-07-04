package net.lulab.drived.persistence.event.sourcing.jpa;

import net.lulab.drived.domain.model.fixture.MusicArtist;
import net.lulab.drived.domain.model.fixture.MusicArtistId;
import net.lulab.drived.event.sourcing.EventStreamId;
import net.lulab.drived.event.sourcing.SnapshotStore;
import net.lulab.drived.persistence.event.sourcing.jpa.fixture.EntityManagerProvider;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class JpaSnapshotStoreTest {

    private SnapshotStore snapshotStore;

    @Before
    public void setup() {
        EntityManagerFactory entityManagerFactory = EntityManagerProvider.getInstance();
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        List<SnapshotEntity> snapshots = em.createQuery(
                "SELECT s from SnapshotEntity s",
                SnapshotEntity.class)
                .getResultList();
        for (SnapshotEntity snapshot : snapshots) {
            em.remove(snapshot);
        }
        tx.commit();

        snapshotStore = new JpaSnapshotStore(entityManagerFactory);
    }

    @Test
    public void snapshot_and_restore_aggregate() {
        EventStreamId eventStreamId = new EventStreamId("test");
        MusicArtist aggregate = new MusicArtist(MusicArtistId.createUniqueId(),
                "Red Velvet",
                ZonedDateTime.parse("2014-08-01T00:00:00+09:00"));
        aggregate.releaseAlbum("The Red", ZonedDateTime.parse("2015-09-09T00:00:00+09:00"));
        aggregate.getPendingEvents().clear();

        snapshotStore.snapshot(
                eventStreamId.withVersion(2L),
                aggregate);
        MusicArtist actual = snapshotStore.restore(
                eventStreamId.withVersion(2L),
                MusicArtist.class);

        assertEquals(2L, actual.getVersion());
        assertEquals(0L, actual.getPendingEvents().size());
        assertEquals(aggregate, actual);
    }

    @Test(expected = IllegalStateException.class)
    public void snapshot_is_failed_when_same_event_stream_id_snapshot() {
        EventStreamId eventStreamId = new EventStreamId("test");
        MusicArtist aggregate = new MusicArtist(MusicArtistId.createUniqueId(),
                "Red Velvet",
                ZonedDateTime.parse("2014-08-01T00:00:00+09:00"));
        aggregate.releaseAlbum("The Red", ZonedDateTime.parse("2015-09-09T00:00:00+09:00"));
        aggregate.getPendingEvents().clear();

        snapshotStore.snapshot(
                eventStreamId.withVersion(2L),
                aggregate);
        snapshotStore.snapshot(
                eventStreamId.withVersion(2L),
                aggregate);

        fail("Should be thrown by the same event stream id");
    }

    @Test
    public void restore_empty_snapshot() {
        EventStreamId eventStreamId = new EventStreamId("test");

        MusicArtist actual = snapshotStore.restore(eventStreamId, MusicArtist.class);

        assertNull(actual);
    }

    @Test
    public void restore_aggregate_is_null_when_not_exist_in_snapshot_store() {
        EventStreamId eventStreamId = new EventStreamId("test");
        MusicArtist aggregate = new MusicArtist(MusicArtistId.createUniqueId(),
                "Red Velvet",
                ZonedDateTime.parse("2014-08-01T00:00:00+09:00"));
        aggregate.releaseAlbum("The Red", ZonedDateTime.parse("2015-09-09T00:00:00+09:00"));
        aggregate.getPendingEvents().clear();

        snapshotStore.snapshot(
                eventStreamId.withVersion(2L),
                aggregate);
        MusicArtist actual = snapshotStore.restore(eventStreamId, MusicArtist.class);

        assertNull(actual);
    }

    @Test
    public void snapshot_is_failed_when_an_aggregate_has_invalid_properties_for_serializing() {
        // TODO: 7/4/18 시리얼라이징이 안되는 코드 추가
    }
}
