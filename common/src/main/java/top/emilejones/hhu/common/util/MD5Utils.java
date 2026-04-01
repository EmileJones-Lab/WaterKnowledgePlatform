package top.emilejones.hhu.common.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * MD5 工具类，用于计算文件内容的 MD5 值。
 */
public class MD5Utils {

    /**
     * 计算指定文件的 MD5 摘要，并返回十六进制字符串表示。
     *
     * @param path 文件路径
     * @return 文件的 MD5 十六进制字符串
     * @throws IOException 如果读取文件时发生 I/O 错误
     */
    public static String calculateMD5(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return DigestUtils.md5Hex(is);
        }
    }

    /**
     * 计算输入流内容的 MD5 摘要，并返回十六进制字符串表示。
     * 调用方负责关闭输入流。
     *
     * @param inputStream 输入流
     * @return 内容的 MD5 十六进制字符串
     * @throws IOException 如果读取流时发生 I/O 错误
     */
    public static String calculateMD5(InputStream inputStream) throws IOException {
        return DigestUtils.md5Hex(inputStream);
    }
}
