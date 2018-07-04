package net.lulab.drived.persistence.event.sourcing.jpa;

import net.lulab.drived.domain.model.fixture.MusicArtistNamed;
import net.lulab.drived.event.DomainEvent;
import net.lulab.drived.event.sourcing.DispatchableDomainEvent;
import net.lulab.drived.event.sourcing.DomainEventSerializer;
import net.lulab.drived.event.sourcing.EventDispatcher;
import net.lulab.drived.persistence.event.sourcing.jackson.JacksonDomainEventSerializer;
import net.lulab.drived.persistence.event.sourcing.jpa.fixture.EntityManagerProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JpaFollowEventDispatcherTest {

    private DomainEventSerializer domainEventSerializer = new JacksonDomainEventSerializer();
    private EntityManagerFactory entityManagerFactory = EntityManagerProvider.getInstance();

    @Before
    public void setup() {
        clearEventsInDatabase();
        append10Events(1L);
    }

    private void clearEventsInDatabase() {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        List<StoredEventEntity> events = em.createQuery(
                "Select t from StoredEventEntity t",
                StoredEventEntity.class)
                .getResultList();
        for (StoredEventEntity event : events) {
            em.remove(event);
        }
        tx.commit();

        em.close();
    }

    @After
    public void teardown() {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        em.clear();
        transaction.commit();
        em.close();
    }

    @Test
    public void dispatch_all_events_when_event_notifier_notifies() {
        ListEventDispatcher eventDispatcher = new ListEventDispatcher();

        JpaFollowEventDispatcher eventNotifier = new JpaFollowEventDispatcher(entityManagerFactory);
        eventNotifier.registerDispatcher(eventDispatcher);
        eventNotifier.notifyDispatchableDomainEvents();

        DispatchableDomainEvent actual = eventDispatcher.getDispatchableDomainEvents().get(1);
        assertEquals(10, eventDispatcher.getDispatchableDomainEvents().size());
        assertEquals("TEST_DISPATCH", actual.getStreamName());
        assertEquals(2L, actual.getVersion());
        assertTrue(Duration.between(actual.getVersionedAt(), ZonedDateTime.now()).toMillis() < 1000);
        assertEquals(new MusicArtistNamed("a2"), actual.getDomainEvent());
    }

    @Test
    public void dispatch_all_events_twice() {
        ListEventDispatcher eventDispatcher = new ListEventDispatcher();

        JpaFollowEventDispatcher eventNotifier = new JpaFollowEventDispatcher(entityManagerFactory);
        eventNotifier.registerDispatcher(eventDispatcher);
        eventNotifier.notifyDispatchableDomainEvents();
        eventDispatcher.getDispatchableDomainEvents().clear();

        append10Events(11L);

        eventNotifier.notifyDispatchableDomainEvents();

        DispatchableDomainEvent actual = eventDispatcher.getDispatchableDomainEvents().get(1);
        assertEquals(10, eventDispatcher.getDispatchableDomainEvents().size());
        assertEquals("TEST_DISPATCH", actual.getStreamName());
        assertEquals(12L, actual.getVersion());
        assertTrue(Duration.between(actual.getVersionedAt(), ZonedDateTime.now()).toMillis() < 1000);
        assertEquals(new MusicArtistNamed("a2"), actual.getDomainEvent());
    }

    @Test
    public void is_able_dispatch_returns_true_always() {
        JpaFollowEventDispatcher eventNotifier = new JpaFollowEventDispatcher(entityManagerFactory);
        boolean actual = eventNotifier.isAbleDispatch(new DispatchableDomainEvent(
                "Test Stream Name",
                1L,
                ZonedDateTime.now(),
                new MusicArtistNamed("Girl friend")
        ));

        assertTrue(actual);
    }

    private void append10Events(long l) {
        List<DomainEvent> events = Arrays.asList(
                new MusicArtistNamed("a1"),
                new MusicArtistNamed("a2"),
                new MusicArtistNamed("a3"),
                new MusicArtistNamed("a4"),
                new MusicArtistNamed("a5"),
                new MusicArtistNamed("a6"),
                new MusicArtistNamed("a7"),
                new MusicArtistNamed("a8"),
                new MusicArtistNamed("a9"),
                new MusicArtistNamed("a10"));

        long expectedVersion = l;
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        for (DomainEvent event : events) {
            em.persist(new StoredEventEntity(
                    "TEST_DISPATCH",
                    expectedVersion,
                    domainEventSerializer.serialize(event)));
            expectedVersion++;
        }
        tx.commit();

        em.close();
    }

    class ListEventDispatcher implements EventDispatcher {
        private List<DispatchableDomainEvent> dispatchableDomainEvents = new ArrayList<>();

        List<DispatchableDomainEvent> getDispatchableDomainEvents() {
            return dispatchableDomainEvents;
        }

        @Override
        public void dispatch(DispatchableDomainEvent event) {
            dispatchableDomainEvents.add(event);
        }

        @Override
        public void registerDispatcher(EventDispatcher dispatcher) {
        }

        @Override
        public boolean isAbleDispatch(DispatchableDomainEvent event) {
            return true;
        }
    }
}