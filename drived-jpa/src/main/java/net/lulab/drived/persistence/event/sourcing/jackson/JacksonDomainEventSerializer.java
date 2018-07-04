package net.lulab.drived.persistence.event.sourcing.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.lulab.drived.event.DomainEvent;
import net.lulab.drived.event.sourcing.DomainEventSerializer;

import java.io.IOException;

public class JacksonDomainEventSerializer implements DomainEventSerializer {

    private final ObjectMapper objectMapper;

    public JacksonDomainEventSerializer() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setDefaultTyping(new JacksonDomainEventTypeResolverBuilder());
    }

    @Override
    public byte[] serialize(DomainEvent event) {
        validateEvent(event);

        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    String.format("DomainEvent may be not serialize `%s`", e.getMessage()),
                    e);
        }
    }

    @Override
    public DomainEvent deserialize(byte[] data) {
        try {
            return objectMapper.readValue(data, DomainEvent.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format("DomainEvent may be not deserialize '%s'", e.getMessage()),
                    e);
        }
    }

    private void validateEvent(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event may be not null");
        }

        if (event.getClass().isAnonymousClass()) {
            throw new IllegalArgumentException("Events may have interface class (Not concrete class)");
        }
    }


}
