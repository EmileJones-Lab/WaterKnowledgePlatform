package top.emilejones.hhu.application;

import org.springframework.stereotype.Service;
import top.emilejones.hhu.application.dto.LazyPageDTO;
import top.emilejones.hhu.application.dto.mission.MissionsDTO;
import top.emilejones.hhu.application.dto.retrieval.TextNodeDTO;

import java.util.List;

@Service
public class DocumentApplicationService {

    /**
     * 获取一个文件的层次结构
     *
     * @param fileId 文件的唯一Id
     * @return 文件结构化数据
     */
    public List<TextNodeDTO> getFileStructureByFileId(String fileId) {
        return null;
    }

    /**
     * 分页的获取已经开启了任意任务的文件信息列表
     *
     * @param limit      每页多少个数据
     * @param pageNum    第几页（从0开始）
     * @param keyword    模糊匹配文件名
     * @param hasMission 是否只返回有任务开启的文件列表
     * @return 任务列表
     */
    public LazyPageDTO<MissionsDTO> getMissionsList(Integer limit, Integer pageNum, String keyword, Boolean hasMission) {
        return null;
    }
}
