package net.lulab.drived.persistence.event.sourcing.jpa.fixture;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerProvider {

    private static final EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("DRIVED_PERSISTENCE_TEST");

    public static EntityManagerFactory getInstance() {
        return entityManagerFactory;
    }

}
