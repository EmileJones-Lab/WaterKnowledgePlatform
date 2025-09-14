package top.emilejones.hhu.web.repository.impl;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;
import top.emilejones.hhu.web.entity.TextNode;
import top.emilejones.hhu.web.enums.TextType;
import top.emilejones.hhu.web.repository.INeo4jRepository;
import top.emilejones.huu.env.pojo.ApplicationConfig;

import java.util.Map;

/**
 * Neo4j仓库的接口实现
 *
 * @author EmileJones
 */
@Repository
public class Neo4jRepository implements INeo4jRepository {
    private final Driver driver;
    private final ApplicationConfig config;

    public Neo4jRepository(ApplicationConfig config) {
        this.config = config;
        String uri = "bolt://%s:%d".formatted(config.getNeo4j().getHost(), config.getNeo4j().getPort());
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(config.getNeo4j().getUser(), config.getNeo4j().getPassword()));
    }

    @Override
    public TextNode nextNode(String elementId) {

        String cypher = """
                MATCH (n:TextNode)-[r:`NEXT_SEQUENCE`]->(m:TextNode)
                WHERE elementId(n) = $elementId
                RETURN m
                """.formatted(elementId);

        Map<String, Object> params = Map.of("elementId", elementId);

        try (Session session = driver.session(SessionConfig.forDatabase(config.getNeo4j().getDatabase()))) {
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

        try (Session session = driver.session(SessionConfig.forDatabase(config.getNeo4j().getDatabase()))) {
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
