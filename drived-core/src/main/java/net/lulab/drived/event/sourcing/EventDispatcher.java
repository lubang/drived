package net.lulab.drived.event.sourcing;

public interface EventDispatcher {

    void dispatch(DispatchableDomainEvent event);

    void registerDispatcher(EventDispatcher dispatcher);

    boolean isAbleDispatch(DispatchableDomainEvent event);

}
