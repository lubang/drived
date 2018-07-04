package net.lulab.drived.domain.model.fixture;

import net.lulab.drived.event.sourcing.AbstractEventProjection;

public class EventHistoryProjection extends AbstractEventProjection {

    private final EventHistoryRepository eventHistoryRepository;

    public EventHistoryProjection(EventHistoryRepository eventHistoryRepository) {
        super();

        this.eventHistoryRepository = eventHistoryRepository;

        addWhen(MusicArtistCreated.class, this::when);
        addWhen(AlbumReleased.class, this::when);
        addWhen(PrizeReceived.class, this::when);
    }

    private void when(MusicArtistCreated event) {
        EventHistory eventHistory = new EventHistory(
                String.format("`%s` artist is debuted", event.getArtistName()),
                event.getDebutedAt());
        eventHistoryRepository.add(eventHistory);
    }

    private void when(AlbumReleased event) {
        EventHistory eventHistory = new EventHistory(
                String.format("`%s` album was released", event.getAlbumName()),
                event.getReleasedAt());
        eventHistoryRepository.add(eventHistory);
    }

    private void when(PrizeReceived event) {
        EventHistory eventHistory = new EventHistory(
                String.format("`%s` prize was received", event.getPrizeName()),
                event.getReceivedAt());
        eventHistoryRepository.add(eventHistory);
    }
}
