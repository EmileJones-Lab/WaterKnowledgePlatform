package top.emilejones.hhu.web.repository.impl;

import kotlin.Pair;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;
import top.emilejones.hhu.web.entity.FileNode;
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
    public Pair<FileNode, TextNode> nextNode(String elementId) {

        String cypher = """
                MATCH (n:TextNode)-[r:`NEXT_SEQUENCE`]->(m:TextNode)
                MATCH (f)-[:CONTAIN]->(m)
                WHERE elementId(n) = $elementId
                RETURN m, f
                """;

        Map<String, Object> params = Map.of("elementId", elementId);

        try (Session session = driver.session(SessionConfig.forDatabase(config.getNeo4j().getDatabase()))) {
            Record record = session.run(
                    cypher,
                    params
            ).single();
            Node textNode = record.get("m").asNode();
            Node fileNode = record.get("f").asNode();
            return new Pair<>(getFileNodeFromNode(fileNode), getTextNodeFromNode(textNode));
        }
    }

    @Override
    public Pair<FileNode, TextNode> preNode(String elementId) {
        String cypher = """
                MATCH (n:TextNode)-[r:`PRE_SEQUENCE`]->(m:TextNode)
                MATCH (f)-[:CONTAIN]->(m)
                WHERE elementId(n) = $elementId
                RETURN m, f
                """;

        Map<String, Object> params = Map.of("elementId", elementId);

        try (Session session = driver.session(SessionConfig.forDatabase(config.getNeo4j().getDatabase()))) {
            Record record = session.run(
                    cypher,
                    params
            ).single();
            Node textNode = record.get("m").asNode();
            Node fileNode = record.get("f").asNode();
            return new Pair<>(getFileNodeFromNode(fileNode), getTextNodeFromNode(textNode));
        }
    }

    @Override
    public Pair<FileNode, TextNode> selectByElementId(String elementId) {
        String cypher = """
                MATCH (n:TextNode)<-[r:CONTAIN]-(f:FileNode)
                WHERE elementId(n) = $elementId
                RETURN n, f
                """;

        Map<String, Object> params = Map.of("elementId", elementId);

        try (Session session = driver.session(SessionConfig.forDatabase(config.getNeo4j().getDatabase()))) {
            Record record = session.run(
                    cypher,
                    params
            ).single();
            Node textNode = record.get("n").asNode();
            Node fileNode = record.get("f").asNode();
            return new Pair<>(getFileNodeFromNode(fileNode), getTextNodeFromNode(textNode));
        }
    }

    private static TextNode getTextNodeFromNode(Node n) {
        TextNode textNode = new TextNode();
        textNode.setElementId(n.elementId());
        textNode.setLevel(n.get("level").asInt());
        textNode.setText(n.get("text").asString());
        textNode.setName(n.get("name").asInt());
        textNode.setType(TextType.valueOf(n.get("type").asString()));
        textNode.setSeq(n.get("seq").asInt());
        return textNode;
    }

    private static FileNode getFileNodeFromNode(Node f) {
        FileNode fileNode = new FileNode();
        fileNode.setFileName(f.get("fileName").asString());
        fileNode.setElementId(f.elementId());
        return fileNode;
    }
}
