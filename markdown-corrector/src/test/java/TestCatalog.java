import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler;
import top.emilejones.hhu.preprocessing.handler.structure.CatalogTitleLevelCorrectorPlus;
import top.emilejones.hhu.preprocessing.structure.TitleTreeExtractor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestCatalog {
    public static void main(String[] args) {
        Path path = Path.of("C:\\Users\\byl\\Desktop\\调度规程md\\屯溪流域枫树岭水库3330127000227  15\\auto\\屯溪流域枫树岭水库3330127000227.md");
        try {
            String originText = Files.readString(path, StandardCharsets.UTF_8);
            MarkdownFileHandler handler = new CatalogTitleLevelCorrectorPlus();
            String result = handler.handle(originText);
            System.out.println(result);
        } catch (IOException e) {
            System.err.println("Failed to read file: " + path);
            e.printStackTrace();
        }
    }
}
