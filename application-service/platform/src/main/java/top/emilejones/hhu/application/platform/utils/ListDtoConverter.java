package top.emilejones.hhu.application.platform.utils;

import top.emilejones.hhu.application.platform.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.platform.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.platform.dto.mission.OcrMissionDTO;
import top.emilejones.hhu.application.platform.dto.retrieval.TextNodeDTO;
import top.emilejones.hhu.domain.result.TextNode;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.List;
import java.util.Optional;

import top.emilejones.hhu.application.platform.dto.mission.MissionsDTO;
import top.emilejones.hhu.domain.document.SourceDocument;
import java.util.ArrayList;

public class ListDtoConverter {
    public static List<MissionsDTO> toMissionsDTOList(
            List<Optional<SourceDocument>> sourceDocuments,
            List<List<OcrMission>> ocrMissions,
            List<List<StructureExtractionMission>> splitterMissions,
            List<List<EmbeddingMission>> embeddingMissions
    ) {
        if (sourceDocuments == null || ocrMissions == null || splitterMissions == null || embeddingMissions == null) {
            return new ArrayList<>();
        }

        List<MissionsDTO> result = new ArrayList<>();
        int size = sourceDocuments.size();

        // Ensure all lists have the same size to avoid IndexOutOfBoundsException
        // Although in expected usage they should match, defensive check is good or assume trusted input.
        // Assuming trusted input order matching from service layer logic.
        
        for (int i = 0; i < size; i++) {
            Optional<SourceDocument> docOptional = sourceDocuments.get(i);
            // Handle case where doc might be null or empty
            if (docOptional.isEmpty()) continue;
            SourceDocument doc = docOptional.get();

            List<OcrMission> ocr = (i < ocrMissions.size()) ? ocrMissions.get(i) : new ArrayList<>();
            List<StructureExtractionMission> split = (i < splitterMissions.size()) ? splitterMissions.get(i) : new ArrayList<>();
            List<EmbeddingMission> embed = (i < embeddingMissions.size()) ? embeddingMissions.get(i) : new ArrayList<>();

            result.add(DtoConverter.toMissionsDTO(doc.getId(), doc.getName(), ocr, split, embed));
        }
        return result;
    }

    public static List<TextNodeDTO> toTextNodeDTOList(List<TextNode> nodes) {
        if (nodes == null) {
            return null;
        }

        return nodes.stream().map(DtoConverter::toTextNodeDTO).toList();
    }

    public static List<EmbeddingMissionDTO> toEmbeddingMissionDTOList(List<EmbeddingMission> mission) {
        if (mission == null) {
            return null;
        }

        return mission.stream().map(DtoConverter::toEmbeddingMissionDTO).toList();
    }

    public static List<OcrMissionDTO> toOcrMissionDTOList(List<OcrMission> mission) {
        if (mission == null) {
            return null;
        }

        return mission.stream().map(DtoConverter::toOcrMissionDTO).toList();
    }

    public static List<DocumentSplittingMissionDTO> toDocumentSplittingMissionDTOList(List<StructureExtractionMission> mission) {
        if (mission == null) {
            return null;
        }

        return mission.stream().map(DtoConverter::toDocumentSplittingMissionDTO).toList();
    }
}
