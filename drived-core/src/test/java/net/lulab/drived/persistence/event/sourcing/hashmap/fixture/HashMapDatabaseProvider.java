package net.lulab.drived.persistence.event.sourcing.hashmap.fixture;

import java.util.HashMap;
import java.util.Map;

public class HashMapDatabaseProvider {

    private static final Map<Long, HashMapStoredEvent> database = new HashMap<>();

    public static Map<Long, HashMapStoredEvent> getDatabase() {
        return database;
    }

}
