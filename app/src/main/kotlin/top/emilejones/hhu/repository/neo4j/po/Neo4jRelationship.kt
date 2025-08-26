package top.emilejones.hhu.repository.neo4j.po

data class Neo4jRelationship(
    val elementId: String? = null,
    val startNodeElementId: String,
    val endNodeElementId: String,
    val type: Neo4jRelationshipType
)
