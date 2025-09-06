package top.emilejones.hhu.repository.neo4j.po

import top.emilejones.hhu.repository.neo4j.enums.Neo4jRelationshipType

data class Neo4jRelationship(
    val elementId: String? = null,
    val startNodeElementId: String,
    val endNodeElementId: String,
    val type: Neo4jRelationshipType
)
