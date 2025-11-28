package top.emilejones.hhu.application.event;

import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;

import java.util.Objects;

public class StartEmbeddingMissionEvent extends MissionEvent {

    private final EmbeddingMission embeddingMission;

    public StartEmbeddingMissionEvent(EmbeddingMission embeddingMission) {
        super();
        this.embeddingMission = Objects.requireNonNull(embeddingMission, "embeddingMission");
    }

    public EmbeddingMission getEmbeddingMission() {
        return embeddingMission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StartEmbeddingMissionEvent)) {
            return false;
        }
        StartEmbeddingMissionEvent that = (StartEmbeddingMissionEvent) o;
        return Objects.equals(embeddingMission, that.embeddingMission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(embeddingMission);
    }

    @Override
    public String toString() {
        return "StartEmbeddingMissionEvent{" +
                "embeddingMission=" + embeddingMission +
                '}';
    }
}
