package top.emilejones.hhu.application.utils;

import top.emilejones.hhu.application.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.dto.mission.OcrMissionDTO;
import top.emilejones.hhu.application.dto.retrieval.TextNodeDTO;
import top.emilejones.hhu.domain.pipeline.TextNode;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.List;

import top.emilejones.hhu.application.dto.mission.MissionsDTO;
import top.emilejones.hhu.domain.document.SourceDocument;
import java.util.ArrayList;

public class ListDtoConverter {
    public static List<MissionsDTO> toMissionsDTOList(
            List<SourceDocument> sourceDocuments,
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
            SourceDocument doc = sourceDocuments.get(i);
            // Handle case where doc might be null (though unlikely from repository usually)
            if (doc == null) continue;

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

        List<EmbeddingMissionDTO> embeddingMissionDTOList = mission.stream().map(DtoConverter::toEmbeddingMissionDTO).toList();
        return embeddingMissionDTOList;
    }

    public static List<OcrMissionDTO> toOcrMissionDTOList(List<OcrMission> mission) {
        if (mission == null) {
            return null;
        }

        List<OcrMissionDTO> ocrMissionDTOList = mission.stream().map(DtoConverter::toOcrMissionDTO).toList();
        return ocrMissionDTOList;
    }

    public static List<DocumentSplittingMissionDTO> toDocumentSplittingMissionDTOList(List<StructureExtractionMission> mission) {
        if (mission == null) {
            return null;
        }

        List<DocumentSplittingMissionDTO> documentSplittingMissionDTO = mission.stream().map(DtoConverter::toDocumentSplittingMissionDTO).toList();
        return documentSplittingMissionDTO;
    }
}
