package top.emilejones.hhu.domain

import java.time.Instant

/**
 * 领域事件基类，记录事件发生时间戳。
 * @author EmileJones
 */
abstract class DomainEvent(
    val occurredOn: Instant = Instant.now()
)
