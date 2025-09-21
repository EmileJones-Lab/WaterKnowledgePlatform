package top.emilejones.hhu.mcp.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.mcp.entity.TextNode;
import top.emilejones.hhu.mcp.repository.INeo4jRepository;

import java.util.List;


@Service
public class Neo4jMcp {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jMcp.class);
    private final INeo4jRepository repository;

    public Neo4jMcp(INeo4jRepository repository) {
        this.repository = repository;
    }

    @Tool(description = "获取当前节点的子节点，并按照先后顺序排序")
    public List<TextNode> getChildren(@ToolParam(description = "当前节点的elementId") String elementId) {
        logger.debug("Try to find children of node[{}]", elementId);
        return repository.children(elementId);
    }

    @Tool(description = "获取当前节点的兄弟节点，并按照先后顺序排序")
    public List<TextNode> getSiblings(@ToolParam(description = "当前节点的elementId") String elementId) {
        logger.debug("Try to find siblings of node[{}]", elementId);
        return repository.siblings(elementId);
    }

    @Tool(description = "获取当前节点的父亲节点")
    public TextNode getParent(@ToolParam(description = "当前节点的elementId") String elementId) {
        logger.debug("Try to find parent of node[{}]", elementId);
        return repository.parent(elementId);
    }

    @Tool(description = "获取当前节点的上一个节点")
    public TextNode getPreNode(@ToolParam(description = "当前节点的elementId") String elementId) {
        logger.debug("Try to find previous node of node[{}]", elementId);
        return repository.parent(elementId);
    }

    @Tool(description = "获取当前节点的下一个节点")
    public TextNode getNextNode(@ToolParam(description = "当前节点的elementId") String elementId) {
        logger.debug("Try to find next node of node[{}]", elementId);
        return repository.parent(elementId);
    }
}

