package top.yeyezhi.hhu.preprocessing.handler;

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler;
import top.emilejones.hhu.preprocessing.handler.structure.*;
import top.yeyezhi.hhu.preprocessing.handler.structure.SubTitleLevelCorrectorReverse;


import java.util.ArrayList;
import java.util.List;

/**
 * 此文件可以使符合`1`=>`1.1`=>`1.1.1`=>`1）`=>`（1）`  这种格式的文件变得层次正确
 *
 * @author yeyezhi
 */
public class MarkdownStructureCorrector3 implements MarkdownFileHandler {
    private static final List<MarkdownFileHandler> chain = new ArrayList<>();

    static {
        chain.add(new PreHandler());
        chain.add(new TitleLevelCorrector());
        chain.add(new SubTitleLevelCorrectorReverse()); // 处理 1）, （1）
        chain.add(new CatalogTitleLevelCorrectorPlus());
        chain.add(new MergeTitleToTextPlus());
    }

    @Override
    public String handle(String markdownText) {
        String text = markdownText;
        for (MarkdownFileHandler handler : chain) {
            text = handler.handle(text);
        }
        return text;
    }
}
