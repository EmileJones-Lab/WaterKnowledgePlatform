package top.emilejones.hhu.web.service.strategy;

import kotlin.Pair;
import top.emilejones.hhu.spliter.java.HtmlTableToCsvSplitterForJava;
import top.emilejones.hhu.web.entity.FileNode;
import top.emilejones.hhu.web.entity.TextNode;
import top.emilejones.hhu.web.enums.TextType;

import java.util.List;

public class HtmlTableToCsvStrategy implements RecallStrategy {

    @Override
    public List<Pair<FileNode, TextNode>> exec(List<Pair<FileNode, TextNode>> rawData) {
        return rawData.stream().map(datum -> {
            if (!TextType.TABLE.equals(datum.getSecond().getType())) {
                return datum;
            }
            List<String> split = HtmlTableToCsvSplitterForJava.INSTANCE.split(datum.getSecond().getText(), Integer.MAX_VALUE);
            datum.getSecond().setText(split.get(0));
            return datum;
        }).toList();
    }
}
