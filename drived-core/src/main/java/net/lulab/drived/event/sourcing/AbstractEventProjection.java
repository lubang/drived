package net.lulab.drived.event.sourcing;

import net.lulab.drived.event.DomainEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractEventProjection implements EventDispatcher {

    private final Map<Class, Consumer<? extends DomainEvent>> whenMap;

    protected AbstractEventProjection() {
        this.whenMap = new HashMap<>();
    }

    protected <T extends DomainEvent> void addWhen(Class<T> eventClass,
                                                   Consumer<T> projector) {
        whenMap.put(eventClass, projector);
    }

    @SuppressWarnings("unchecked")
    private void project(DomainEvent event) {
        Class<? extends DomainEvent> eventClass = event.getClass();
        Consumer consumer = whenMap.get(eventClass);
        if (consumer != null) {
            consumer.accept(event);
        }
    }

    @Override
    public void dispatch(DispatchableDomainEvent event) {
        project(event.getDomainEvent());
    }

    @Override
    public void registerDispatcher(EventDispatcher dispatcher) {
        throw new UnsupportedOperationException("Cannot register additional dispatchers");
    }

    @Override
    public boolean isAbleDispatch(DispatchableDomainEvent event) {
        return whenMap.containsKey(event.getDomainEvent().getClass());
    }
}
