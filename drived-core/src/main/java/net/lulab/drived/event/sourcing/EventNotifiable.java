package net.lulab.drived.event.sourcing;

public interface EventNotifiable {

    void notifyDispatchableDomainEvents();
}
