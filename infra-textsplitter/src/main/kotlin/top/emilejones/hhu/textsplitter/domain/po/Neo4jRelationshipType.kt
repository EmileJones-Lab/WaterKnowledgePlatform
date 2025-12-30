package top.emilejones.hhu.textsplitter.domain.po

enum class Neo4jRelationshipType(val comment: String) {
    PARENT("父节点"),
    CHILD("子节点"),
    NEXT_SEQUENCE("文章的上一个节点"),
    PRE_SEQUENCE("文章的下一个节点"),
    CONTAIN("包含节点")
}