package top.emilejones.hhu.web.service.strategy;

import kotlin.Pair;
import top.emilejones.hhu.web.entity.FileNode;
import top.emilejones.hhu.web.entity.TextNode;

import java.util.List;

public interface RecallStrategy {
    List<Pair<FileNode, TextNode>> exec(List<Pair<FileNode, TextNode>> rawData);
}
