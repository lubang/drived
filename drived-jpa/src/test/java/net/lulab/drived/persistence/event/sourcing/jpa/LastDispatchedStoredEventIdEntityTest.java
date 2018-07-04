package net.lulab.drived.persistence.event.sourcing.jpa;

import net.lulab.drived.persistence.event.sourcing.jpa.fixture.EntityManagerProvider;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import static org.junit.Assert.assertEquals;

public class LastDispatchedStoredEventIdEntityTest {

    @Test
    public void get_id_is_1L() {
        LastDispatchedStoredEventIdEntity actual1 = new LastDispatchedStoredEventIdEntity(2L);
        LastDispatchedStoredEventIdEntity actual2 = new LastDispatchedStoredEventIdEntity(30L);

        assertEquals(1L, actual1.getId());
        assertEquals(2L, actual1.getLastDispatchedEventId());
        assertEquals(1L, actual2.getId());
        assertEquals(30L, actual2.getLastDispatchedEventId());
    }

    @Test
    public void default_constructor_for_entity() {
        LastDispatchedStoredEventIdEntity actual = new LastDispatchedStoredEventIdEntity();

        assertEquals(1L, actual.getId());
        assertEquals(0L, actual.getLastDispatchedEventId());
    }

    @Test
    public void persist_and_find() {
        EntityManagerFactory entityManagerFactory = EntityManagerProvider.getInstance();
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        em.merge(new LastDispatchedStoredEventIdEntity(2L));
        tx.commit();

        LastDispatchedStoredEventIdEntity actual = em.find(
                LastDispatchedStoredEventIdEntity.class,
                LastDispatchedStoredEventIdEntity.FIXED_ID);
        em.close();

        assertEquals(1L, actual.getId());
        assertEquals(2L, actual.getLastDispatchedEventId());
    }
}