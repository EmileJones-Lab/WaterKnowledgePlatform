package top.emilejones.hhu.domain

import java.time.Instant

abstract class DomainEvent(
    val occurredOn: Instant = Instant.now()
)