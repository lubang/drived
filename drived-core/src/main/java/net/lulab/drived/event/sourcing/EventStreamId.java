package net.lulab.drived.event.sourcing;

import java.util.Objects;

public final class EventStreamId {

    private final String streamName;

    private final long version;

    public EventStreamId(String streamName, long version) {
        this.streamName = streamName;
        this.version = version;
    }

    public EventStreamId(String streamName) {
        this(streamName, 1L);
    }

    public EventStreamId withVersion(long version) {
        return new EventStreamId(this.getStreamName(), version);
    }

    public String getStreamName() {
        return streamName;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventStreamId that = (EventStreamId) o;
        return version == that.version &&
                Objects.equals(streamName, that.streamName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamName, version);
    }
}
