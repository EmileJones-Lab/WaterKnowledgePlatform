package top.emilejones.hhu.application.event;

import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;

import java.util.Objects;

public class StartOcrMissionEvent extends MissionEvent {

    private final OcrMission ocrMission;

    public StartOcrMissionEvent(OcrMission ocrMission) {
        super();
        this.ocrMission = Objects.requireNonNull(ocrMission, "ocrMission");
    }

    public OcrMission getOcrMission() {
        return ocrMission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StartOcrMissionEvent)) {
            return false;
        }
        StartOcrMissionEvent that = (StartOcrMissionEvent) o;
        return Objects.equals(ocrMission, that.ocrMission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ocrMission);
    }

    @Override
    public String toString() {
        return "StartOcrMissionEvent{" +
                "ocrMission=" + ocrMission +
                '}';
    }
}
