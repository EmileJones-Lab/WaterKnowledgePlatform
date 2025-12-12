package top.emilejones.hhu.application;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.application.dto.LazyPageDTO;
import top.emilejones.hhu.application.dto.knowledge.KnowledgeDirectoryDTO;
import top.emilejones.hhu.application.dto.knowledge.KnowledgeFileDTO;
import top.emilejones.hhu.application.dto.knowledge.request.AddKnowledgeDirectoryDTO;
import top.emilejones.hhu.application.dto.knowledge.request.AddKnowledgeFileDTO;
import top.emilejones.hhu.application.dto.knowledge.request.UpdateKnowledgeDirectoryDTO;
import top.emilejones.hhu.domain.pipeline.event.EmbeddingMissionSuccessEvent;

import java.util.List;

@Service
public class KnowledgeApplicationService {

    /**
     * 获取知识库列表
     *
     * @return 知识库列表
     */
    public List<KnowledgeDirectoryDTO> getAllKnowledgeDirectories() {
        return null;
    }

    /**
     * 新增一个知识库
     *
     * @param request 新增请求
     * @return 新增的知识库元数据
     */
    public KnowledgeDirectoryDTO addKnowledgeDirectory(AddKnowledgeDirectoryDTO request) {
        return null;
    }

    /**
     * 修改一个知识库元信息
     *
     * @param id      知识库唯一Id
     * @param request 修改请求
     * @return 修改后的文件夹元信息
     */
    public KnowledgeDirectoryDTO updateKnowledgeDirectory(String id, UpdateKnowledgeDirectoryDTO request) {
        return null;
    }

    /**
     * 删除一个知识库
     *
     * @param id 知识库唯一Id
     */
    public void deleteKnowledgeDirectory(String id) {

    }

    /**
     * 获取知识库中的知识文件详细信息列表
     *
     * @param dirId   知识库唯一Id
     * @param limit   每页多少个数据
     * @param pageNum 第几页（从0开始）
     * @param keyword 根据文件名模糊匹配
     * @return 知识文件详细信息列表
     */
    public LazyPageDTO<KnowledgeFileDTO> getAllKnowledgeFileByDirId(String dirId, Integer limit, Integer pageNum, String keyword) {
        return null;
    }

    /**
     * 向知识库中添加一个知识文件
     *
     * @param dirId   知识库唯一Id
     * @param request 添加请求
     * @return 知识文件的元信息
     */
    public KnowledgeFileDTO addKnowledgeFileByDirId(String dirId, AddKnowledgeFileDTO request) {
        return null;
    }

    /**
     * 删除知识库中的一个知识文件
     *
     * @param dirId       知识库唯一Id
     * @param embeddingId 知识文件唯一Id
     */
    public void deleteKnowledgeFileByDirId(String dirId, String embeddingId) {

    }

    /**
     * 获取可以加入到这个知识库中的文件列表
     *
     * @param dirId 知识库唯一Id
     * @return 文件列表
     */
    public LazyPageDTO<KnowledgeFileDTO> getAllCandidateFiles(String dirId) {
        return null;
    }

    /**
     * 监听EmbeddingMissionSuccessEvent事件，将成功向量化后的文档添加到知识库。
     *
     * @param event 向量化任务成功事件
     */
    @EventListener
    public void addAKnowledgeDocumentFromSuccessfulEmbeddingMission(EmbeddingMissionSuccessEvent event) {

    }
}
