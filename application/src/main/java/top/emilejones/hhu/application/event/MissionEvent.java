package top.emilejones.hhu.application.event;

import java.time.Instant;

public abstract class MissionEvent {
    private final Instant eventTime;

    public MissionEvent() {
        eventTime = Instant.now();
    }

    public Instant getEventTime() {
        return eventTime;
    }
}
