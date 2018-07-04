package net.lulab.drived.domain.model.fixture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.Objects;

@AllArgsConstructor
@Getter
public class EventHistory {

    @NonNull
    private String message;

    @NonNull
    private ZonedDateTime occurredAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventHistory that = (EventHistory) o;
        return Objects.equals(message, that.message) &&
                occurredAt.isEqual(that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, occurredAt);
    }
}
