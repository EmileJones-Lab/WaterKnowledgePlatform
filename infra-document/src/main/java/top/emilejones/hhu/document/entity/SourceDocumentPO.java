package top.emilejones.hhu.document.entity;
import lombok.Data;

/**
 * 源文件对象信息
 * @author Yeyezhi
 */
@Data
public class SourceDocumentPO {
    private int id;
    private String filename;
    private String catapath;
    private String filepath;
    private String filetype;
}
