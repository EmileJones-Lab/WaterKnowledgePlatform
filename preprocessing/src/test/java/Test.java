import top.yeyezhi.hhu.preprocessing.structure.TitleTreeExtractor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Simple test entry that reads a local file and normalizes its Markdown structure.
 * Usage: run with the file path as the first argument.
 */
public class Test {
    public static void main(String[] args) {

        Path path = Path.of("C:\\Users\\byl\\Desktop\\调度规程md\\屯溪流域丰乐水库(大坝)3341004000441  13\\auto\\屯溪流域丰乐水库(大坝)3341004000441.md");
        try {
            String originText = Files.readString(path, StandardCharsets.UTF_8);
            TitleTreeExtractor extractor = new TitleTreeExtractor();
            String result = extractor.extract(originText);
            System.out.println(result);
        } catch (IOException e) {
            System.err.println("Failed to read file: " + path);
            e.printStackTrace();
        }
    }
}
