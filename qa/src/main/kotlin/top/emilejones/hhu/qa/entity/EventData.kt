package top.emilejones.hhu.qa.entity

data class EventData(
    val event: String,
    val data: WorkflowData?
)

data class WorkflowData(
    val outputs: Outputs?
)

data class Outputs(
    val qaList: String,
    val status: String
)
