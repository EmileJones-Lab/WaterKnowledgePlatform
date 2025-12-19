package top.emilejones.hhu.application.utils;

import top.emilejones.hhu.application.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.dto.mission.OcrMissionDTO;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.List;

public class ListDtoConverter {
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
