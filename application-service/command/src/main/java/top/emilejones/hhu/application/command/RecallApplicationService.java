package top.emilejones.hhu.application.command;

import org.springframework.stereotype.Service;
import top.emilejones.hhu.application.command.dto.FileNodeDTO;
import top.emilejones.hhu.application.command.dto.TextNodeDTO;
import top.emilejones.hhu.application.command.record.ProcessRecordService;
import top.emilejones.hhu.domain.pipeline.repository.FileNodeVectorRepository;
import top.emilejones.hhu.domain.pipeline.repository.TextNodeVectorRepository;
import top.emilejones.hhu.domain.result.TextNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 召回应用服务。
 * 负责从向量数据库中召回与查询最相关的文本内容。
 */
@Service
public class RecallApplicationService {

    private final TextNodeVectorRepository textNodeVectorRepository;
    private final FileNodeVectorRepository fileNodeVectorRepository;
    private final ProcessRecordService processRecordService;

    public RecallApplicationService(
            TextNodeVectorRepository textNodeVectorRepository,
            FileNodeVectorRepository fileNodeVectorRepository,
            ProcessRecordService processRecordService) {
        this.textNodeVectorRepository = textNodeVectorRepository;
        this.fileNodeVectorRepository = fileNodeVectorRepository;
        this.processRecordService = processRecordService;
    }

    /**
     * 根据查询语句召回相关的文本内容。
     * 逻辑：先召回相关的文件，再在这些文件中召回文本。
     *
     * @param query 查询字符串
     * @return 与查询最相关的文本列表
     */
    public List<String> recallText(String query) {
        // 1. 先根据问题召回相关文件的 fileNodeId
        List<String> fileNodeIds = recallFileNodes(query).stream()
                .map(FileNodeDTO::getId)
                .collect(Collectors.toList());

        // 2. 在指定的文件范围内召回相关的 textNode
        List<TextNode> textNodes = textNodeVectorRepository.recallTextNode(
                query,
                EmbeddingApplicationService.COLLECTION_NAME,
                fileNodeIds
        );

        return textNodes.stream()
                .map(TextNode::getText)
                .collect(Collectors.toList());
    }

    /**
     * 根据查询语句召回相关的文本节点。
     * 逻辑：先召回相关的文件，再在这些文件中召回文本节点。
     *
     * @param query 查询字符串
     * @return 与查询最相关的文本节点列表
     */
    public List<TextNodeDTO> recallTextNodes(String query) {
        // 1. 先根据问题召回相关文件的 fileNodeId
        List<String> fileNodeIds = recallFileNodes(query).stream()
                .map(FileNodeDTO::getId)
                .collect(Collectors.toList());

        // 2. 在指定的文件范围内召回相关的 textNode
        List<TextNode> textNodes = textNodeVectorRepository.recallTextNode(
                query,
                EmbeddingApplicationService.COLLECTION_NAME,
                fileNodeIds
        );

        return textNodes.stream()
                .map(node -> TextNodeDTO.builder()
                        .id(node.getId())
                        .fileNodeId(node.getFileNodeId())
                        .text(node.getText())
                        .seq(node.getSeq())
                        .level(node.getLevel())
                        .type(node.getType().name())
                        .fileName(processRecordService.getFileNameByFileNodeId(node.getFileNodeId()))
                        .summary(node.getSummary())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 根据用户问题召回相关的 FileNode。
     *
     * @param query 用户问题
     * @return 与问题相关的 FileNode 列表
     */
    public List<FileNodeDTO> recallFileNodes(String query) {
        return fileNodeVectorRepository.recallFileNode(
                        query,
                        EmbeddingApplicationService.FILE_COLLECTION_NAME
                ).stream()
                .map(node -> FileNodeDTO.builder()
                        .id(node.getId())
                        .sourceDocumentId(node.getSourceDocumentId())
                        .isEmbedded(node.isEmbedded())
                        .fileAbstract(node.getFileAbstract())
                        .fileName(processRecordService.getFileNameByFileNodeId(node.getId()))
                        .build())
                .collect(Collectors.toList());
    }
}
