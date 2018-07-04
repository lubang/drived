package net.lulab.drived.persistence.event.sourcing.jpa;

import net.lulab.drived.domain.model.fixture.*;
import net.lulab.drived.event.sourcing.EventDispatcher;
import net.lulab.drived.event.sourcing.EventStore;
import net.lulab.drived.event.sourcing.EventStoreConcurrencyException;
import net.lulab.drived.event.sourcing.EventStreamId;
import net.lulab.drived.persistence.event.sourcing.jpa.fixture.EntityManagerProvider;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class EventProjectionTest {
    private JpaFollowEventDispatcher jpaFollowEventDispatcher;
    private EventStore eventStore;

    private MusicArtistId id;

    @Before
    public void setup() {
        EntityManagerFactory entityManagerFactory = EntityManagerProvider.getInstance();
        clearEventsInDatabase(entityManagerFactory);

        jpaFollowEventDispatcher = new JpaFollowEventDispatcher(entityManagerFactory);
        eventStore = new JpaEventStore(entityManagerFactory);
        eventStore.registerEventNotifiable(jpaFollowEventDispatcher);

        id = MusicArtistId.createUniqueId();
    }

    private void clearEventsInDatabase(EntityManagerFactory entityManagerFactory) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        List<StoredEventEntity> events = entityManager.createQuery(
                "SELECT e FROM StoredEventEntity e",
                StoredEventEntity.class)
                .getResultList();
        for (StoredEventEntity event : events) {
            entityManager.remove(event);
        }
        tx.commit();
        entityManager.close();
    }

    @Test
    public void event_history_projection_invokes_when_music_artist_raise_an_event()
            throws EventStoreConcurrencyException {

        EventHistoryRepository eventHistoryRepository = mock(EventHistoryRepository.class);
        jpaFollowEventDispatcher.registerDispatcher(new EventHistoryProjection(eventHistoryRepository));

        MusicArtist redVelvet = new MusicArtist(
                id,
                "Red Velvet",
                ZonedDateTime.parse("2014-08-01T00:00:00+09:00"));
        eventStore.appendWith(
                new EventStreamId(id.getId()),
                redVelvet.getPendingEvents());

        verify(eventHistoryRepository, times(1))
                .add(eq(new EventHistory(
                        "`Red Velvet` artist is debuted",
                        ZonedDateTime.parse("2014-08-01T00:00:00+09:00"))));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void event_project_does_not_register_dispatcher() {
        EventHistoryRepository eventHistoryRepository = mock(EventHistoryRepository.class);
        EventHistoryProjection eventHistoryProjection = new EventHistoryProjection(eventHistoryRepository);
        eventHistoryProjection.registerDispatcher(mock(EventDispatcher.class));

        fail("EventHistoryProjection can not register an event dispatcher");
    }
}