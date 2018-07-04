package net.lulab.drived.persistence.event.sourcing.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LastDispatchedStoredEventIdEntity {

    public static final long FIXED_ID = 1L;

    @Id
    private final long id;

    private final long lastDispatchedEventId;

    public LastDispatchedStoredEventIdEntity() {
        this.id = FIXED_ID;
        this.lastDispatchedEventId = 0L;
    }

    public LastDispatchedStoredEventIdEntity(long lastDispatchedEventId) {
        this.id = FIXED_ID;
        this.lastDispatchedEventId = lastDispatchedEventId;
    }

    public long getId() {
        return id;
    }

    public long getLastDispatchedEventId() {
        return lastDispatchedEventId;
    }
}
