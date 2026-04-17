package top.emilejones.hhu.textsplitter.domain.po.neo4j

data class Neo4jRelationship(
    val elementId: String? = null,
    val startNodeElementId: String,
    val endNodeElementId: String,
    val type: Neo4jRelationshipType
)
