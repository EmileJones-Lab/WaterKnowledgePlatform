package top.emilejones.hhu.application.event;

import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.Objects;

public class StartStructureExtractionMissionEvent extends MissionEvent {

    private final StructureExtractionMission structureExtractionMission;

    public StartStructureExtractionMissionEvent(StructureExtractionMission structureExtractionMission) {
        super();
        this.structureExtractionMission =
                Objects.requireNonNull(structureExtractionMission, "structureExtractionMission");
    }

    public StructureExtractionMission getStructureExtractionMission() {
        return structureExtractionMission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StartStructureExtractionMissionEvent)) {
            return false;
        }
        StartStructureExtractionMissionEvent that = (StartStructureExtractionMissionEvent) o;
        return Objects.equals(structureExtractionMission, that.structureExtractionMission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structureExtractionMission);
    }

    @Override
    public String toString() {
        return "StartStructureExtractionMissionEvent{" +
                "structureExtractionMission=" + structureExtractionMission +
                '}';
    }
}
