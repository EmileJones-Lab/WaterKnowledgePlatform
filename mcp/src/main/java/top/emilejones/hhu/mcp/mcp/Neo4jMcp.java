package top.emilejones.hhu.mcp.mcp;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.mcp.entity.TextNode;
import top.emilejones.hhu.mcp.enums.TextType;
import top.emilejones.huu.env.pojo.ApplicationConfig;

import java.util.Map;
import java.util.Set;


@Service
public class Neo4jMcp implements AutoCloseable {

    private static Logger logger = LoggerFactory.getLogger(Neo4jMcp.class);
    private final Driver driver;
    private final ApplicationConfig config;

    public Neo4jMcp(ApplicationConfig config) {
        this.config = config;
        // 修改为Neo4j 地址和密码
        String uri = "bolt://%s:%d".formatted(config.getNeo4j().getHost(), config.getNeo4j().getPort());
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(config.getNeo4j().getUser(), config.getNeo4j().getPassword()));
    }

    /**
     * 根据 elementId 和关系，返回指定的一个节点
     *
     * @param elementId    起始节点 ID
     * @param relationship 关系类型（CHILD / PARENT / NEXT_SEQUENCE / PRE_SEQUENCE）
     * @param step         跳数
     * @param index        返回第几个匹配的节点（从 0 开始）
     * @return 单个节点 JSON
     */
    @Tool(description = """
            根据起始节点 ID 查找相关节点。
            每个节点都有 elementId 和 text 属性，其中 elementId 用于支持多次递归调用，text 是文本内容，用于 AI 查询资料时获取上下文。
            如果某个片段信息不足，可以调用此工具根据该片段查找上下文，并结合上下文生成答案。
            """)
    public TextNode searchNodeByRelation(
            @ToolParam(description = "起始节点ID") String elementId,
            @ToolParam(description = "需要查找的关系，包括如下关系：父亲：PARENT，孩子：CHILD，上一个：PRE_SEQUENCE，下一个：NEXT_SEQUENCE") String relationship,
            @ToolParam(description = "跳数") int step,
            @ToolParam(description = "查询出的列表的第几个元素") int index) {
        // 验证关系类型
        Set<String> validRelationships = Set.of("CHILD", "PARENT", "NEXT_SEQUENCE", "PRE_SEQUENCE");
        if (!validRelationships.contains(relationship)) {
            throw new IllegalArgumentException("Invalid relationship type: " + relationship);
        }

        if (step < 0) {
            throw new IllegalArgumentException("Step must be non-negative");
        }

        if (index < 0) {
            throw new IllegalArgumentException("Index must be non-negative");
        }

        try (Session session = driver.session()) {
            String cypherQuery;
            if (step == 0) {
                // 直接返回起始节点
                cypherQuery = "MATCH (targetNode) WHERE elementId(targetNode) = $elementId RETURN targetNode";
            } else {
                // 构建关系查询
                cypherQuery = String.format(
                        "MATCH (startNode)-[:%s*%d]->(targetNode) " +
                                "WHERE elementId(startNode) = $elementId " +
                                "RETURN targetNode " +
                                "SKIP $index LIMIT 1",
                        relationship, step
                );
            }

            Map<String, Object> params = Map.of(
                    "elementId", elementId,
                    "index", index
            );

            return session.readTransaction(tx -> {
                Result result = tx.run(cypherQuery, params);
                if (!result.hasNext()) {
                    logger.warn("No node found for elementId: {}, relationship: {}, step: {}, index: {}",
                            elementId, relationship, step, index);
                    return null;
                }

                Record record = result.single();
                Node node = record.get("targetNode").asNode();

                TextNode textNode = new TextNode();
                textNode.setElementId(node.elementId());
                textNode.setText(node.get("text").asString());
                textNode.setLevel(node.get("level").asInt());
                textNode.setSeq(node.get("seq").asInt());
                textNode.setName(node.get("name").asInt());
                textNode.setType(TextType.valueOf(node.get("type").asString()));


                return textNode;
            });
        } catch (Exception e) {
            logger.error("Error executing Neo4j query", e);
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }
}

