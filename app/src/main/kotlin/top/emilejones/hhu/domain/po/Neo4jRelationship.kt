package top.emilejones.hhu.domain.po

import top.emilejones.hhu.domain.enums.Neo4jRelationshipType

data class Neo4jRelationship(
    val elementId: String? = null,
    val startNodeElementId: String,
    val endNodeElementId: String,
    val type: Neo4jRelationshipType
)
