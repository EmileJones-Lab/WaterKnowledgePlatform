package top.emilejones.hhu.mcp.repository.impl;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Repository;
import top.emilejones.hhu.mcp.entity.TextNode;
import top.emilejones.hhu.mcp.enums.TextType;
import top.emilejones.hhu.mcp.repository.INeo4jRepository;
import top.emilejones.huu.env.pojo.ApplicationConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Neo4j仓库的接口实现
 *
 * @author EmileJones
 */
@Repository
public class Neo4jRepository implements INeo4jRepository, AutoCloseable {
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
                """;

        Map<String, Object> params = Map.of("elementId", elementId);

        try (Session session = driver.session(SessionConfig.forDatabase(config.getNeo4j().getDatabase()))) {
            Record record = session.run(
                    cypher,
                    params
            ).single();
            Node m = record.get("m").asNode();
            return fromNode(m);
        }
    }

    @Override
    public TextNode preNode(String elementId) {
        String cypher = """
                MATCH (n:TextNode)-[r:`PRE_SEQUENCE`]->(m:TextNode)
                WHERE elementId(n) = $elementId
                RETURN m
                """;

        Map<String, Object> params = Map.of("elementId", elementId);

        try (Session session = driver.session(SessionConfig.forDatabase(config.getNeo4j().getDatabase()))) {
            Record record = session.run(
                    cypher,
                    params
            ).single();
            Node m = record.get("m").asNode();
            return fromNode(m);
        }
    }

    @Override
    public TextNode parent(String elementId) {
        String cypher = """
                MATCH (n:TextNode)-[r:`PARENT`]->(m:TextNode)
                WHERE elementId(n) = $elementId
                RETURN m
                """;

        Map<String, Object> params = Map.of("elementId", elementId);

        try (Session session = driver.session(SessionConfig.forDatabase(config.getNeo4j().getDatabase()))) {
            Result result = session.run(
                    cypher,
                    params
            );
            if (!result.hasNext())
                return null;
            Record record = result.single();
            Node m = record.get("m").asNode();
            return fromNode(m);
        }
    }

    @Override
    public List<TextNode> siblings(String elementId) {
        String findParentCypher = """
                MATCH (n:TextNode)-[r:`PARENT`]->(m:TextNode)
                WHERE elementId(n) = $elementId
                RETURN m
                """;
        String findChildCypher = """
                MATCH (n:TextNode)-[r:`CHILD`]->(m:TextNode)
                WHERE elementId(n) = $elementId
                RETURN m
                ORDER BY m.seq ASC;
                """;


        try (Session session = driver.session(SessionConfig.forDatabase(config.getNeo4j().getDatabase()))) {
            Map<String, Object> findParentParams = Map.of("elementId", elementId);
            Result parentResult = session.run(
                    findParentCypher,
                    findParentParams
            );
            // 如果不存在父亲节点，则返回空
            if (!parentResult.hasNext()) {
                return new ArrayList<>();
            }
            // 如果存在父亲节点则查找父亲节点的孩子，就是此节点的兄弟
            Record record = parentResult.single();
            Node m = record.get("m").asNode();
            TextNode parentNode = fromNode(m);

            Map<String, Object> findChildrenParams = Map.of("elementId", parentNode.getElementId());
            Result childrenResult = session.run(
                    findChildCypher,
                    findChildrenParams
            );
            return childrenResult.list().stream()
                    .map(r -> r.get("m").asNode())
                    .map(Neo4jRepository::fromNode)
                    .toList();
        }
    }

    @Override
    public List<TextNode> children(String elementId) {
        String cypher = """
                MATCH (n:TextNode)-[r:`CHILD`]->(m:TextNode)
                WHERE elementId(n) = $elementId
                RETURN m
                ORDER BY m.seq ASC;
                """;

        Map<String, Object> params = Map.of("elementId", elementId);

        try (Session session = driver.session(SessionConfig.forDatabase(config.getNeo4j().getDatabase()))) {
            Result result = session.run(
                    cypher,
                    params
            );
            ArrayList<TextNode> textNodes = new ArrayList<>();
            for (Record record : result.list()) {
                Node m = record.get("m").asNode();
                textNodes.add(fromNode(m));
            }
            return textNodes;
        }
    }

    private static TextNode fromNode(Node node) {
        TextNode textNode = new TextNode();
        textNode.setElementId(node.elementId());
        textNode.setLevel(node.get("level").asInt());
        textNode.setText(node.get("text").asString());
        textNode.setName(node.get("name").asInt());
        textNode.setType(TextType.valueOf(node.get("type").asString()));
        textNode.setSeq(node.get("seq").asInt());
        return textNode;
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }
}
