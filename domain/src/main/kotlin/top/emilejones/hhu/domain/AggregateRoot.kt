package top.emilejones.hhu.domain

abstract class AggregateRoot<ID>(
    open val id: ID
) {
    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    protected fun raiseEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    fun pushEvents(): List<DomainEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()
        return events
    }
}