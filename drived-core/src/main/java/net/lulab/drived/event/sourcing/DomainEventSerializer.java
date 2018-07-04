package net.lulab.drived.event.sourcing;

import net.lulab.drived.event.DomainEvent;

public interface DomainEventSerializer {

    byte[] serialize(DomainEvent event);

    DomainEvent deserialize(byte[] data);

}
