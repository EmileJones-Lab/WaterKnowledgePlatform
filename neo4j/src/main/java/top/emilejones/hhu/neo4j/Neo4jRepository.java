package top.emilejones.hhu.neo4j;

import kotlin.Pair;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import top.emilejones.hhu.entity.FileNode;
import top.emilejones.hhu.entity.TextNode;
import top.emilejones.hhu.enums.TextType;
import top.emilejones.hhu.repository.INeo4jRepository;

import java.util.Map;

/**
 * Neo4j仓库的接口实现
 *
 * @author EmileJones
 */
public class Neo4jRepository implements INeo4jRepository {
    private final Driver driver;
    private final String databaseName;

    public Neo4jRepository(String host, Integer port, String user, String password, String databaseName) {
        String uri = "bolt://%s:%d".formatted(host, port);
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        this.databaseName = databaseName;
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

        try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
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

        try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
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

        try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
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
