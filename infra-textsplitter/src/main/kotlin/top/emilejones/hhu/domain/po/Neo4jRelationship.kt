package top.emilejones.hhu.domain.po

data class Neo4jRelationship(
    val elementId: String? = null,
    val startNodeElementId: String,
    val endNodeElementId: String,
    val type: Neo4jRelationshipType
)
