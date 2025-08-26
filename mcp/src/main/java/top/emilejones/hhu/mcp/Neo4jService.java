package top.emilejones.hhu.mcp;


import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import top.emilejones.huu.env.Neo4jEnvironment;

import java.util.Map;


@Service
public class Neo4jService implements AutoCloseable {

    private static Logger logger = LoggerFactory.getLogger(Neo4jService.class);
    private final Driver driver;

    public Neo4jService() {
        // 修改为Neo4j 地址和密码
        String uri = "bolt://%s:%d".formatted(Neo4jEnvironment.HOST, Neo4jEnvironment.PORT);
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(Neo4jEnvironment.USER, Neo4jEnvironment.PASSWORD));
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
        if (elementId == null || elementId.isBlank()) {
            return null;
        }
        if (relationship == null || relationship.isBlank()) {
            relationship = "CHILD";
        }
        int hops = Math.max(step, 1);
        int pick = Math.max(index, 0);

        // 构造 Cypher
        String cypher = String.format("MATCH (start:TextNode) " + "WHERE elementId(start) = $elementId " + "MATCH (start)-[:%s*%d]->(target:TextNode) " + "WITH target ORDER BY coalesce(target.seq,0) ASC " + "SKIP toInteger($skip) LIMIT 1 " + "RETURN target", relationship, hops);


        try (Session session = driver.session()) {
            Map<String, Object> params = Map.of("elementId", elementId, "skip", pick);

            logger.debug("Mcp exec cypher: [{}], param: [{}]", cypher, params);

            Result rs = session.run(cypher, params);

            if (rs.hasNext()) {
                Record r = rs.next();
                Node node = r.get("target").asNode();

                return new TextNode(node.elementId(), node.containsKey("id") ? node.get("id").asInt() : null, node.containsKey("level") ? node.get("level").asInt() : null, node.containsKey("seq") ? node.get("seq").asInt() : null, node.containsKey("name") ? node.get("name").asInt() : null, node.containsKey("text") ? node.get("text").asString() : null);
            }


        } catch (Exception e) {
            throw new RuntimeException("Neo4j 查询失败", e);
        }

        return null; // 没有结果
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }
}

