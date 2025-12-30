package top.emilejones.hhu.domain

/**
 * 基础聚合根，负责聚合的唯一标识及领域事件收集。
 * @author EmileJones
 */
abstract class AggregateRoot<ID>(
    open val id: ID
) {
    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    /**
     * 记录一个新的领域事件，待外部统一发布。
     */
    fun raiseEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    /**
     * 推送并清空当前聚合内的领域事件。
     */
    fun pushEvents(): List<DomainEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()
        return events
    }
}
