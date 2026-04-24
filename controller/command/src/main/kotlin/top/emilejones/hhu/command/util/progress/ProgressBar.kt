package top.emilejones.hhu.command.util.progress

/**
 * 纯函数式进度条渲染器，无状态，线程安全。
 *
 * 使用 Unicode 全宽块字符构建进度条，提供清晰的视觉反馈。
 */
object ProgressBar {
    const val DEFAULT_WIDTH = 30
    private const val FULL_BLOCK = "█"
    private const val EMPTY_BLOCK = "░"

    /**
     * 将进度值（0.0 ~ 1.0）渲染为进度条字符串。
     *
     * @param progress 进度值，范围 [0.0, 1.0]，超出范围会被钳制
     * @param width 进度条宽度（字符数）
     * @return 格式化后的进度条字符串，例如："██████████░░░░░░░░░░░░░░░░░░░░  33%"
     */
    fun render(progress: Double, width: Int = DEFAULT_WIDTH): String {
        val clamped = progress.coerceIn(0.0, 1.0)
        val filled = (clamped * width).toInt()
        val empty = width - filled
        return buildString {
            append(FULL_BLOCK.repeat(filled))
            append(EMPTY_BLOCK.repeat(empty))
            append(" %3d%%".format((clamped * 100).toInt()))
        }
    }
}
