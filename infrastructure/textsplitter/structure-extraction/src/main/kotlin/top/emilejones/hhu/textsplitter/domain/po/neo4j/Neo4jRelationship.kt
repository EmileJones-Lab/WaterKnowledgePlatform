package top.emilejones.hhu.textsplitter.domain.po.neo4j

data class Neo4jRelationship(
    val startNodeId: String,
    val endNodeId: String,
    val type: Neo4jRelationshipType
)
