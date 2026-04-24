package top.emilejones.hhu.command.util.progress

import java.io.PrintStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 批量任务进度管理器（线程安全）。
 *
 * 基于真实的已完成文件数与总文件数的比例渲染进度条，
 * 不使用时间估算。同时显示当前正在处理的文件名，让用户知道系统正在工作。
 *
 * 使用示例：
 * ```
 * val manager = BatchProgressManager(mdFiles.size)
 * manager.start()
 *
 * runBlocking {
 *     mdFiles.map { file ->
 *         launch(Dispatchers.IO) {
 *             manager.addTask(file.name)
 *             try {
 *                 process(file)
 *             } finally {
 *                 manager.completeTask(file.name)
 *             }
 *         }
 *     }.joinAll()
 * }
 * ```
 *
 * @property totalFiles 总文件数
 * @property output 输出流
 * @property refreshIntervalMs 刷新间隔（毫秒）
 */
class BatchProgressManager(
    private val totalFiles: Int,
    private val output: PrintStream = System.out,
    private val refreshIntervalMs: Long = 300L
) {
    private val completedCount = AtomicInteger(0)
    private val activeFiles = ConcurrentHashMap.newKeySet<String>()
    private val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "batch-progress-display").apply { isDaemon = true }
    }
    private val stopped = AtomicBoolean(false)
    private var future: ScheduledFuture<*>? = null

    /**
     * 启动批量进度刷新线程。
     */
    fun start() {
        future = scheduler.scheduleAtFixedRate(
            { refreshDisplay() },
            0,
            refreshIntervalMs,
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * 注册一个新任务到活跃集合中。
     *
     * @param fileName 文件名（用作标识和显示）
     */
    fun addTask(fileName: String) {
        activeFiles.add(fileName)
    }

    /**
     * 标记指定任务已完成。
     *
     * 从活跃集合中移除，并递增完成计数。
     * 当所有任务都完成时，自动停止刷新。
     *
     * @param fileName 文件名
     */
    fun completeTask(fileName: String) {
        activeFiles.remove(fileName)
        val done = completedCount.incrementAndGet()
        if (done >= totalFiles) {
            stop()
        }
    }

    /**
     * 强制停止进度刷新，并输出最终状态。
     *
     * 通常在 [completeTask] 自动触发，也可手动调用。
     */
    fun stop() {
        if (stopped.compareAndSet(false, true)) {
            future?.cancel(false)
            scheduler.shutdown()
            val done = completedCount.get()
            val bar = ProgressBar.render(done.toDouble() / totalFiles, 20)
            output.print("\r[Batch $done/$totalFiles] $bar")
            output.println()
        }
    }

    private fun refreshDisplay() {
        if (stopped.get()) return

        val done = completedCount.get()
        val pct = done.toDouble() / totalFiles
        val bar = ProgressBar.render(pct, 20)

        // 显示一个当前活跃的文件名
        val active = activeFiles.firstOrNull() ?: ""
        val line = if (active.isNotEmpty()) {
            "\r[Batch $done/$totalFiles] $bar | $active"
        } else {
            "\r[Batch $done/$totalFiles] $bar"
        }

        output.print(line)
    }
}
