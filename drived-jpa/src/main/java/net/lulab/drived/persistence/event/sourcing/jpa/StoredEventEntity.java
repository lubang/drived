package net.lulab.drived.persistence.event.sourcing.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Entity
public class StoredEventEntity {

    @Id
    @GeneratedValue
    private long eventId;

    private String streamName;

    private long version;

    private ZonedDateTime versionedAt;

    private byte[] data;

    protected StoredEventEntity() {
    }

    public StoredEventEntity(String streamName,
                             long version,
                             byte[] data) {
        this();
        this.streamName = streamName;
        this.version = version;
        this.versionedAt = ZonedDateTime.now();
        this.data = data;
    }

    public long getEventId() {
        return eventId;
    }

    public String getStreamName() {
        return streamName;
    }

    public long getVersion() {
        return version;
    }

    public ZonedDateTime getVersionedAt() {
        return versionedAt;
    }

    public byte[] getData() {
        return data;
    }
}
