package net.lulab.drived.persistence.event.sourcing.jpa;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class SnapshotEntity {

    @EmbeddedId
    private SnapshotEntityId id;

    @Lob
    private byte[] data;

    protected SnapshotEntity() {
    }

    public SnapshotEntity(SnapshotEntityId id, byte[] data) {
        this();
        this.id = id;
        this.data = data;
    }

    public SnapshotEntityId getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }
}
