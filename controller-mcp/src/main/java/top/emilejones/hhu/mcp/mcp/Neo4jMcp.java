package top.emilejones.hhu.mcp.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.textsplitter.domain.po.Neo4jTextNode;
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository;

import java.util.List;


@Service
public class Neo4jMcp {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jMcp.class);
    private final INeo4jRepository repository;

    public Neo4jMcp(INeo4jRepository repository) {
        this.repository = repository;
    }

    @Tool(description = "获取指定节点的所有子节点（下一级标题或属于这个标题的正文），并按文档顺序返回。如果没有数据，则返回null")
    public List<Neo4jTextNode> getChildren(@ToolParam(description = "当前节点的elementId") String elementId) {
        logger.debug("Try to find children of node[{}]", elementId);
        List<Neo4jTextNode> children = repository.children(elementId);
        if (children.isEmpty())
            return null;
        return children;
    }

    @Tool(description = "获取指定节点所属的标题下的所有兄弟节点，并按文档顺序返回。")
    public List<Neo4jTextNode> getSiblings(@ToolParam(description = "当前节点的elementId") String elementId) {
        logger.debug("Try to find siblings of node[{}]", elementId);
        return repository.siblings(elementId);
    }

    @Tool(description = "获取指定节点在文档层级结构中的父节点（所属标题节点），若不存在则返回 null。")
    public Neo4jTextNode getParent(@ToolParam(description = "当前节点的elementId") String elementId) {
        logger.debug("Try to find parent of node[{}]", elementId);
        return repository.parent(elementId);
    }

    @Tool(description = "根据当前节点elementId，获取文档中的上一段内容的节点数据，若不存在则返回 null。")
    public Neo4jTextNode getPreNode(@ToolParam(description = "当前节点的elementId") String elementId) {
        logger.debug("Try to find previous node of node[{}]", elementId);
        return repository.parent(elementId);
    }

    @Tool(description = "根据当前节点elementId，获取文档中的下一段内容的节点数据，若不存在则返回 null。")
    public Neo4jTextNode getNextNode(@ToolParam(description = "当前节点的elementId") String elementId) {
        logger.debug("Try to find next node of node[{}]", elementId);
        return repository.parent(elementId);
    }
}

