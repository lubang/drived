package net.lulab.drived.persistence.event.sourcing.jpa;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class SnapshotEntityId implements Serializable {

    private String streamName;

    private long version;

    protected SnapshotEntityId() {
    }

    public SnapshotEntityId(String streamName, long version) {
        this();
        this.setStreamName(streamName);
        this.setVersion(version);
    }

    public String getStreamName() {
        return streamName;
    }

    public long getVersion() {
        return version;
    }

    private void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    private void setVersion(long version) {
        this.version = version;
    }
}
