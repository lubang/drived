package net.lulab.drived.event.sourcing;

public class EventStoreConcurrencyException extends Throwable {

    public EventStoreConcurrencyException(long expectedStreamVersion,
                                          long actualStreamVersion,
                                          String streamName) {
        super(String.format("Expected version %d in stream '%s' but got %d", expectedStreamVersion,
                streamName,
                actualStreamVersion));
    }
}
