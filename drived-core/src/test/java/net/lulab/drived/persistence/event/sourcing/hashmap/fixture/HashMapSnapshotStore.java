package net.lulab.drived.persistence.event.sourcing.hashmap.fixture;

import net.lulab.drived.domain.model.AggregateRoot;
import net.lulab.drived.event.sourcing.EventStreamId;
import net.lulab.drived.event.sourcing.SnapshotStore;

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class HashMapSnapshotStore implements SnapshotStore {

    private final Map<EventStreamId, byte[]> snapshots;

    public HashMapSnapshotStore() {
        snapshots = new HashMap<>();
    }

    @Override
    public <T extends AggregateRoot> void snapshot(EventStreamId eventStreamId, T aggregate) {
        try {
            snapshots.put(eventStreamId, serialize(aggregate));
        } catch (Exception e) {
            throw new IllegalStateException("Fail to snapshot", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AggregateRoot> T restore(EventStreamId eventStreamId, Class<T> aggregateClass) {
        byte[] data = snapshots.keySet().stream()
                .filter(k -> isLowerVersionInStream(eventStreamId, k))
                .sorted(Comparator.comparingLong(EventStreamId::getVersion))
                .limit(1)
                .findFirst()
                .map(snapshots::get)
                .orElse(null);
        if (data == null) {
            return null;
        }

        try {
            return deserialize(data);
        } catch (Exception e) {
            throw new IllegalStateException("Fail to restore", e);
        }
    }

    private boolean isLowerVersionInStream(EventStreamId baseEventStreamId,
                                           EventStreamId targetEventStreamId) {

        return targetEventStreamId.getStreamName().equals(baseEventStreamId.getStreamName())
                && targetEventStreamId.getVersion() <= baseEventStreamId.getVersion();
    }

    @Override
    public boolean isNeedSnapshot(EventStreamId eventStreamId) {
        return eventStreamId.getVersion() % 2 == 1;
    }

    private <T extends AggregateRoot> byte[] serialize(T aggregate)
            throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(aggregate);
        oos.flush();
        return bos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private <T extends AggregateRoot> T deserialize(byte[] data)
            throws IOException, ClassNotFoundException {

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (T) ois.readObject();
    }
}
