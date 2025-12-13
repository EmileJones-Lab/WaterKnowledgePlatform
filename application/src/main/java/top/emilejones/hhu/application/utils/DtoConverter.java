package top.emilejones.hhu.application.utils;

import top.emilejones.hhu.application.dto.knowledge.KnowledgeDirectoryDTO;
import top.emilejones.hhu.application.dto.knowledge.KnowledgeDirectoryType;
import top.emilejones.hhu.application.dto.knowledge.KnowledgeFileDTO;
import top.emilejones.hhu.application.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.dto.mission.MissionsDTO;
import top.emilejones.hhu.application.dto.mission.OcrMissionDTO;
import top.emilejones.hhu.application.dto.mission.enums.DocumentSplittingMissionType;
import top.emilejones.hhu.application.dto.mission.enums.MissionStatus;
import top.emilejones.hhu.application.dto.retrieval.TextNodeDTO;
import top.emilejones.hhu.application.dto.retrieval.TextType;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.domain.pipeline.TextNode;

import java.util.Collections;
import java.util.List;

public class DtoConverter {

    public static DocumentSplittingMissionDTO toDocumentSplittingMissionDTO(StructureExtractionMission mission) {
        if (mission == null) {
            return null;
        }
        DocumentSplittingMissionDTO dto = new DocumentSplittingMissionDTO();
        dto.setExtractStructureMissionId(mission.getId());
        dto.setStartTime(mission.getStartTime());
        dto.setEndTime(mission.getEndTime());
        dto.setType(null); // Not directly available from domain mission
        dto.setStatus(mapMissionStatus(mission.getStatus()));
        if (mission.getStatus() == top.emilejones.hhu.domain.pipeline.MissionStatus.ERROR) {
            try {
                dto.setRemark(mission.getFailureResult().getErrorMessage());
            } catch (Exception e) {
                dto.setRemark("Unknown Error");
            }
        }
        return dto;
    }

    public static EmbeddingMissionDTO toEmbeddingMissionDTO(EmbeddingMission mission) {
        if (mission == null) {
            return null;
        }
        EmbeddingMissionDTO dto = new EmbeddingMissionDTO();
        dto.setEmbeddingMissionId(mission.getId());
        dto.setStartTime(mission.getStartTime());
        dto.setEndTime(mission.getEndTime());
        dto.setStatus(mapMissionStatus(mission.getStatus()));
        if (mission.getStatus() == top.emilejones.hhu.domain.pipeline.MissionStatus.ERROR) {
            try {
                 dto.setRemark(mission.getFailureResult().getErrorMessage());
            } catch (Exception e) {
                dto.setRemark("Unknown Error");
            }
        }
        return dto;
    }

    public static OcrMissionDTO toOcrMissionDTO(OcrMission mission) {
        if (mission == null) {
            return null;
        }
        OcrMissionDTO dto = new OcrMissionDTO();
        dto.setOcrMissionId(mission.getId());
        dto.setStartTime(mission.getStartTime());
        dto.setEndTime(mission.getEndTime());
        dto.setStatus(mapMissionStatus(mission.getStatus()));
        if (mission.getStatus() == top.emilejones.hhu.domain.pipeline.MissionStatus.ERROR) {
            try {
                dto.setRemark(mission.getFailureResult().getErrorMessage());
            } catch (Exception e) {
                dto.setRemark("Unknown Error");
            }
        }
        return dto;
    }

    public static KnowledgeDirectoryDTO toKnowledgeDirectoryDTO(KnowledgeCatalog catalog) {
        if (catalog == null) {
            return null;
        }
        KnowledgeDirectoryDTO dto = new KnowledgeDirectoryDTO();
        dto.setId(catalog.getId());
        dto.setKbName(catalog.getName());
        dto.setColName(catalog.getMilvusCollectionName());
        dto.setCreateTime(null); // Assuming createTime is not in KnowledgeCatalog yet or handled elsewhere
        dto.setType(mapKnowledgeCatalogType(catalog.getType()));
        return dto;
    }

    public static KnowledgeFileDTO toKnowledgeFileDTO(KnowledgeDocument document) {
        if (document == null) {
            return null;
        }
        KnowledgeFileDTO dto = new KnowledgeFileDTO();
        dto.setEmbeddingMissionId(document.getEmbeddingMissionId());
        dto.setFileName(document.getName());
        dto.setCreateTime(document.getCreateTime());
        dto.setType(mapKnowledgeDocumentType(document.getType()));
        // These lists need to be populated separately as they are not in KnowledgeDocument
        dto.setOcrMission(Collections.emptyList());
        dto.setExtractStructureMission(Collections.emptyList());
        dto.setEmbeddingMission(Collections.emptyList());
        return dto;
    }

    public static TextNodeDTO toTextNodeDTO(TextNode node) {
        if (node == null) {
            return null;
        }
        TextNodeDTO dto = new TextNodeDTO();
        dto.setElementId(node.getId());
        dto.setText(node.getText());
        dto.setSeq(node.getSeq());
        dto.setLevel(node.getLevel());
        dto.setType(mapTextType(node.getType()));
        return dto;
    }

    public static MissionsDTO toMissionsDTO(String fileId, String fileName, List<OcrMission> ocrMissions, List<StructureExtractionMission> extractionMissions, List<EmbeddingMission> embeddingMissions) {
        MissionsDTO dto = new MissionsDTO();
        dto.setFileId(fileId);
        dto.setFileName(fileName);
        dto.setOcrMission(ocrMissions.stream().map(DtoConverter::toOcrMissionDTO).toList());
        dto.setExtractStructureMission(extractionMissions.stream().map(DtoConverter::toDocumentSplittingMissionDTO).toList());
        dto.setEmbeddingMission(embeddingMissions.stream().map(DtoConverter::toEmbeddingMissionDTO).toList());
        return dto;
    }

    public static MissionStatus mapMissionStatus(top.emilejones.hhu.domain.pipeline.MissionStatus domainStatus) {
        return switch (domainStatus) {
            case CREATED, PENDING -> MissionStatus.PENDING;
            case RUNNING -> MissionStatus.RUNNING;
            case ERROR -> MissionStatus.ERROR;
            case SUCCESS -> MissionStatus.SUCCESS;
        };
    }

    private static KnowledgeDirectoryType mapKnowledgeCatalogType(KnowledgeCatalogType type) {
        return switch (type) {
            case CHAR_NUMBER_SPLIT_DIR -> KnowledgeDirectoryType.CHAR_NUMBER_SPLIT_DIR;
            case STRUCTURE_KNOWLEDGE_DIR -> KnowledgeDirectoryType.STRUCTURE_KNOWLEDGE_DIR;
        };
    }
    
    public static KnowledgeCatalogType mapKnowledgeDirectoryDTOType(KnowledgeDirectoryType type) {
        return switch (type) {
            case CHAR_NUMBER_SPLIT_DIR -> KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR;
            case STRUCTURE_KNOWLEDGE_DIR -> KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR;
        };
    }

    private static DocumentSplittingMissionType mapKnowledgeDocumentType(KnowledgeDocumentType type) {
        return switch (type) {
            case CHAR_LENGTH_SPLITTER_200 -> DocumentSplittingMissionType.CHAR_LENGTH_SPLITTER_200;
            case CHAR_LENGTH_SPLITTER_400 -> DocumentSplittingMissionType.CHAR_LENGTH_SPLITTER_400;
            case CHAR_LENGTH_SPLITTER_600 -> DocumentSplittingMissionType.CHAR_LENGTH_SPLITTER_600;
            case STRUCTURE_SPLITTER -> DocumentSplittingMissionType.STRUCTURE_SPLITTER;
        };
    }

    private static TextType mapTextType(top.emilejones.hhu.domain.pipeline.TextType type) {
         return switch (type) {
            case COMMON_TEXT -> TextType.COMMON_TEXT;
            case TABLE -> TextType.TABLE;
            case IMAGE -> TextType.IMAGE;
            case TITLE -> TextType.TITLE;
            case NULL -> TextType.COMMON_TEXT; // Mapping NULL to COMMON_TEXT or handle as needed
        };
    }
}
