package top.emilejones.hhu.preprocessing.handler;

import org.jetbrains.annotations.NotNull;
import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler;
import top.emilejones.hhu.preprocessing.handler.structure.*;
import top.emilejones.hhu.preprocessing.handler.structure.MixedTitleLevelCorrector;

import java.util.ArrayList;
import java.util.List;


/**
 * 此文件可以使符合`一、` => `（二）` => `3.` => `（4）` => `5）`这种格式的文件变得层次正确
 *
 * @author yeyezhi
 */
public class MixedStructureCorrector implements MarkdownFileHandler {

    private static final List<MarkdownFileHandler> chain = new ArrayList<>();

    static {
        chain.add(new PreHandler());
        chain.add(new MixedTitleLevelCorrector()); // 处理 一、 → （二） → 3. → （4） → 5）
        //chain.add(new SubTitleLevelCorrector()); // 处理 （1）, 1）
        //chain.add(new CatalogTitleLevelCorrector());
        chain.add(new MergeTitleToText());
    }

    @NotNull
    @Override
    public String handle(@NotNull String markdownText) {
        String text = markdownText;
        for (MarkdownFileHandler handler : chain) {
            text = handler.handle(text);
        }
        return text;
    }

}
