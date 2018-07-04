package net.lulab.drived.persistence.event.sourcing.jackson;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;

class JacksonDomainEventTypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {

    JacksonDomainEventTypeResolverBuilder() {
        super(ObjectMapper.DefaultTyping.NON_FINAL);
        init(JsonTypeInfo.Id.CLASS, null);
        inclusion(JsonTypeInfo.As.PROPERTY);
        typeProperty("@event");
    }

    @Override
    public boolean useForType(JavaType t) {
        return !t.isPrimitive() && t.getRawClass() != ZonedDateTime.class;
    }

}
