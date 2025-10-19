package top.yeyezhi.hhu.preprocessing.handler;

import org.jetbrains.annotations.NotNull;
import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler;
import top.emilejones.hhu.preprocessing.handler.structure.CatalogTitleLevelCorrectorPlus;
import top.emilejones.hhu.preprocessing.handler.structure.PreHandler;
import top.yeyezhi.hhu.preprocessing.handler.structure.MixedTitleLevelCorrector4;

import java.util.ArrayList;
import java.util.List;


/**
 * 此文件可以使符合`一、`  => `1.1` => `1.1.1` =>`1、`  这种格式的文件变得层次正确
 *
 * @author yeyezhi
 */
public class MixedStructureCorrector4 implements MarkdownFileHandler {

    private static final List<MarkdownFileHandler> chain = new ArrayList<>();

    static {
        chain.add(new PreHandler());
        chain.add(new MixedTitleLevelCorrector4()); // 处理 `一、`  => `1.1` => `1.1.1` =>`1、`
        chain.add(new CatalogTitleLevelCorrectorPlus());
        //chain.add(new MergeTitleToTextPlus());
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