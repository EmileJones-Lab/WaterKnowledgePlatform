package top.emilejones.hhu.command.util.progress

import java.io.PrintStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 不确定型任务的旋转指示器（Spinner）。
 *
 * 用于无法预估处理时间的场景（如数据库查询、网络请求），
 * 通过旋转的 Braille 字符提供视觉反馈，让用户感知系统正在工作。
 *
 * 使用示例：
 * ```
 * val spinner = Spinner("正在检索相关内容...")
 * spinner.start()
 * try {
 *     val result = queryDatabase()
 * } finally {
 *     spinner.stop("完成")
 * }
 * ```
 *
 * @property label 显示在 spinner 旁边的说明文字
 * @property output 输出流
 * @property refreshIntervalMs 旋转间隔（毫秒）
 */
class Spinner(
    private val label: String,
    private val output: PrintStream = System.out,
    private val refreshIntervalMs: Long = 100L
) {
    private val frames = listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
    private var frameIndex = 0
    private val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "spinner-$label").apply { isDaemon = true }
    }
    private val stopped = AtomicBoolean(false)

    /**
     * 启动旋转动画。
     */
    fun start() {
        scheduler.scheduleAtFixedRate(
            {
                if (stopped.get()) return@scheduleAtFixedRate
                val frame = frames[frameIndex % frames.size]
                output.print("\r$frame $label")
                frameIndex++
            },
            0,
            refreshIntervalMs,
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * 停止旋转动画，输出完成标记并换行。
     *
     * @param message 完成时显示的附加消息，例如 "完成"、"成功"
     */
    fun stop(message: String = "完成") {
        if (stopped.compareAndSet(false, true)) {
            scheduler.shutdown()
            output.print("\r✓ $label $message")
            output.println()
        }
    }
}
