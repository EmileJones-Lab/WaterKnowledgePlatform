package top.emilejones.hhu.web.utils;

import top.emilejones.hhu.application.dto.knowledge.CandidateKnowledgeFileDTO;
import top.emilejones.hhu.application.dto.knowledge.KnowledgeDirectoryDTO;
import top.emilejones.hhu.application.dto.knowledge.KnowledgeFileDTO;
import top.emilejones.hhu.application.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.dto.mission.MissionsDTO;
import top.emilejones.hhu.application.dto.mission.OcrMissionDTO;
import top.emilejones.hhu.application.dto.retrieval.TextNodeDTO;
import top.emilejones.hhu.web.vo.knowledge.CandidateKnowledgeFileVO;
import top.emilejones.hhu.web.vo.knowledge.KnowledgeDirectoryVO;
import top.emilejones.hhu.web.vo.knowledge.KnowledgeFileVO;
import top.emilejones.hhu.web.vo.mission.DocumentSplittingMissionVO;
import top.emilejones.hhu.web.vo.mission.EmbeddingMissionVO;
import top.emilejones.hhu.web.vo.mission.MissionsVO;
import top.emilejones.hhu.web.vo.mission.OcrMissionVO;
import top.emilejones.hhu.web.vo.retrieval.TextNodeVO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VoConverter {

    public static TextNodeVO toTextNodeVO(TextNodeDTO dto) {
        if (dto == null) return null;
        TextNodeVO vo = new TextNodeVO();
        vo.setElementId(dto.getId());
        vo.setText(dto.getText());
        vo.setSeq(dto.getSeq());
        vo.setLevel(dto.getLevel());
        vo.setType(toTextType(dto.getType()));
        return vo;
    }

    public static List<TextNodeVO> toTextNodeVOList(List<TextNodeDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream().map(VoConverter::toTextNodeVO).collect(Collectors.toList());
    }

    public static MissionsVO toMissionsVO(MissionsDTO dto) {
        if (dto == null) return null;
        MissionsVO vo = new MissionsVO();
        vo.setFileId(dto.getFileId());
        vo.setFileName(dto.getFileName());
        vo.setOcrMission(toOcrMissionVOList(dto.getOcrMission()));
        vo.setExtractStructureMission(toDocumentSplittingMissionVOList(dto.getExtractStructureMission()));
        vo.setEmbeddingMission(toEmbeddingMissionVOList(dto.getEmbeddingMission()));
        return vo;
    }

    public static List<MissionsVO> toMissionsVOList(List<MissionsDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream().map(VoConverter::toMissionsVO).collect(Collectors.toList());
    }

    public static OcrMissionVO toOcrMissionVO(OcrMissionDTO dto) {
        if (dto == null) return null;
        OcrMissionVO vo = new OcrMissionVO();
        vo.setOcrMissionId(dto.getOcrMissionId());
        vo.setStatus(toMissionStatus(dto.getStatus()));
        vo.setRemark(dto.getRemark());
        vo.setCreateTime(dto.getCreateTime());
        vo.setStartTime(dto.getStartTime());
        vo.setEndTime(dto.getEndTime());
        return vo;
    }

    public static List<OcrMissionVO> toOcrMissionVOList(List<OcrMissionDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream().map(VoConverter::toOcrMissionVO).collect(Collectors.toList());
    }

    public static DocumentSplittingMissionVO toDocumentSplittingMissionVO(DocumentSplittingMissionDTO dto) {
        if (dto == null) return null;
        DocumentSplittingMissionVO vo = new DocumentSplittingMissionVO();
        vo.setExtractStructureMissionId(dto.getExtractStructureMissionId());
        vo.setStatus(toMissionStatus(dto.getStatus()));
        vo.setType(toDocumentSplittingMissionType(dto.getType()));
        vo.setRemark(dto.getRemark());
        vo.setCreateTime(dto.getCreateTime());
        vo.setStartTime(dto.getStartTime());
        vo.setEndTime(dto.getEndTime());
        return vo;
    }

    public static List<DocumentSplittingMissionVO> toDocumentSplittingMissionVOList(List<DocumentSplittingMissionDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream().map(VoConverter::toDocumentSplittingMissionVO).collect(Collectors.toList());
    }

    public static EmbeddingMissionVO toEmbeddingMissionVO(EmbeddingMissionDTO dto) {
        if (dto == null) return null;
        EmbeddingMissionVO vo = new EmbeddingMissionVO();
        vo.setEmbeddingMissionId(dto.getEmbeddingMissionId());
        vo.setStatus(toMissionStatus(dto.getStatus()));
        vo.setRemark(dto.getRemark());
        vo.setCreateTime(dto.getCreateTime());
        vo.setStartTime(dto.getStartTime());
        vo.setEndTime(dto.getEndTime());
        return vo;
    }

    public static List<EmbeddingMissionVO> toEmbeddingMissionVOList(List<EmbeddingMissionDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream().map(VoConverter::toEmbeddingMissionVO).collect(Collectors.toList());
    }

    public static KnowledgeDirectoryVO toKnowledgeDirectoryVO(KnowledgeDirectoryDTO dto) {
        if (dto == null) return null;
        KnowledgeDirectoryVO vo = new KnowledgeDirectoryVO();
        vo.setId(dto.getId());
        vo.setColName(dto.getColName());
        vo.setKbName(dto.getKbName());
        vo.setCreateTime(dto.getCreateTime());
        vo.setType(toKnowledgeDirectoryType(dto.getType()));
        return vo;
    }
    
    public static List<KnowledgeDirectoryVO> toKnowledgeDirectoryVOList(List<KnowledgeDirectoryDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream().map(VoConverter::toKnowledgeDirectoryVO).collect(Collectors.toList());
    }

    public static KnowledgeFileVO toKnowledgeFileVO(KnowledgeFileDTO dto) {
        if (dto == null) return null;
        KnowledgeFileVO vo = new KnowledgeFileVO();
        vo.setKnowledgeFileId(dto.getId());
        vo.setType(toDocumentSplittingMissionType(dto.getType()));
        vo.setFileName(dto.getFileName());
        vo.setBindTime(dto.getBindTime());
        vo.setOcrMission(toOcrMissionVOList(dto.getOcrMission()));
        vo.setExtractStructureMission(toDocumentSplittingMissionVOList(dto.getExtractStructureMission()));
        vo.setEmbeddingMission(toEmbeddingMissionVOList(dto.getEmbeddingMission()));
        return vo;
    }

    public static List<KnowledgeFileVO> toKnowledgeFileVOList(List<KnowledgeFileDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream().map(VoConverter::toKnowledgeFileVO).collect(Collectors.toList());
    }

    public static CandidateKnowledgeFileVO toCandidateKnowledgeFileVO(CandidateKnowledgeFileDTO dto) {
        if (dto == null) return null;
        CandidateKnowledgeFileVO vo = new CandidateKnowledgeFileVO();
        vo.setKnowledgeFileId(dto.getId());
        vo.setType(toDocumentSplittingMissionType(dto.getType()));
        vo.setFileName(dto.getFileName());
        vo.setCreateTime(dto.getCreateTime());
        vo.setOcrMission(toOcrMissionVOList(dto.getOcrMission()));
        vo.setExtractStructureMission(toDocumentSplittingMissionVOList(dto.getExtractStructureMission()));
        vo.setEmbeddingMission(toEmbeddingMissionVOList(dto.getEmbeddingMission()));
        return vo;
    }
    
    public static List<CandidateKnowledgeFileVO> toCandidateKnowledgeFileVOList(List<CandidateKnowledgeFileDTO> dtos) {
         if (dtos == null) return Collections.emptyList();
        return dtos.stream().map(VoConverter::toCandidateKnowledgeFileVO).collect(Collectors.toList());
    }

    public static top.emilejones.hhu.web.vo.retrieval.TextType toTextType(top.emilejones.hhu.application.dto.retrieval.TextType type) {
        if (type == null) return null;
        return top.emilejones.hhu.web.vo.retrieval.TextType.valueOf(type.name());
    }

    public static top.emilejones.hhu.web.vo.mission.enums.MissionStatus toMissionStatus(top.emilejones.hhu.application.dto.mission.enums.MissionStatus status) {
        if (status == null) return null;
        return top.emilejones.hhu.web.vo.mission.enums.MissionStatus.valueOf(status.name());
    }
    
    public static top.emilejones.hhu.web.vo.mission.enums.DocumentSplittingMissionType toDocumentSplittingMissionType(top.emilejones.hhu.application.dto.mission.enums.DocumentSplittingMissionType type) {
        if (type == null) return null;
        return top.emilejones.hhu.web.vo.mission.enums.DocumentSplittingMissionType.valueOf(type.name());
    }

    public static top.emilejones.hhu.web.vo.knowledge.KnowledgeDirectoryType toKnowledgeDirectoryType(top.emilejones.hhu.application.dto.knowledge.KnowledgeDirectoryType type) {
        if (type == null) return null;
        return top.emilejones.hhu.web.vo.knowledge.KnowledgeDirectoryType.valueOf(type.name());
    }

    public static top.emilejones.hhu.application.dto.knowledge.KnowledgeDirectoryType toKnowledgeDirectoryDTOType(top.emilejones.hhu.web.vo.knowledge.KnowledgeDirectoryType type) {
        if (type == null) return null;
        return top.emilejones.hhu.application.dto.knowledge.KnowledgeDirectoryType.valueOf(type.name());
    }

    public static top.emilejones.hhu.application.dto.knowledge.request.AddKnowledgeDirectoryDTO toAddKnowledgeDirectoryDTO(top.emilejones.hhu.web.vo.knowledge.request.AddKnowledgeDirectoryRequest request) {
        if (request == null) return null;
        top.emilejones.hhu.application.dto.knowledge.request.AddKnowledgeDirectoryDTO dto = new top.emilejones.hhu.application.dto.knowledge.request.AddKnowledgeDirectoryDTO();
        dto.setDirName(request.getDirName());
        dto.setType(toKnowledgeDirectoryDTOType(request.getType()));
        return dto;
    }
}
