package top.emilejones.hhu.web.repository.impl;

import org.neo4j.driver.Record;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;
import top.emilejones.hhu.web.entity.TextNode;
import top.emilejones.hhu.web.enums.TextType;
import top.emilejones.hhu.web.repository.INeo4jRepository;
import top.emilejones.huu.env.Neo4jEnvironment;

import java.util.Map;

/**
 * Neo4j仓库的接口实现
 *
 * @author EmileJones
 */
@Repository
public class Neo4jRepository implements INeo4jRepository {
    private final Driver driver;

    public Neo4jRepository() {
        // 修改为Neo4j 地址和密码
        String uri = "bolt://%s:%d".formatted(Neo4jEnvironment.HOST, Neo4jEnvironment.PORT);
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(Neo4jEnvironment.USER, Neo4jEnvironment.PASSWORD));
    }

    @Override
    public TextNode nextNode(String elementId) {

        String cypher = """
                MATCH (n:TextNode)-[r:`NEXT_SEQUENCE`]->(m:TextNode)
                WHERE elementId(n) = $elementId
                RETURN m
                """.formatted(elementId);

        Map<String, Object> params = Map.of("elementId", elementId);

        try (Session session = driver.session(SessionConfig.forDatabase(Neo4jEnvironment.DATABASE))) {
            Record record = session.run(
                    cypher,
                    params
            ).single();
            Node m = record.get("m").asNode();
            TextNode textNode = new TextNode();
            textNode.setElementId(m.elementId());
            textNode.setLevel(m.get("level").asInt());
            textNode.setText(m.get("text").asString());
            textNode.setName(m.get("name").asInt());
            textNode.setType(TextType.valueOf(m.get("type").asString()));
            textNode.setSeq(m.get("seq").asInt());
            return textNode;
        }
    }

    @Override
    public TextNode preNode(String elementId) {
        String cypher = """
                MATCH (n:TextNode)-[r:`PRE_SEQUENCE`]->(m:TextNode)
                WHERE elementId(n) = $elementId
                RETURN m
                """.formatted(elementId);

        Map<String, Object> params = Map.of("elementId", elementId);

        try (Session session = driver.session(SessionConfig.forDatabase(Neo4jEnvironment.DATABASE))) {
            Record record = session.run(
                    cypher,
                    params
            ).single();
            Node m = record.get("m").asNode();
            TextNode textNode = new TextNode();
            textNode.setElementId(m.elementId());
            textNode.setLevel(m.get("level").asInt());
            textNode.setText(m.get("text").asString());
            textNode.setName(m.get("name").asInt());
            textNode.setType(TextType.valueOf(m.get("type").asString()));
            textNode.setSeq(m.get("seq").asInt());
            return textNode;
        }
    }
}
