package top.emilejones.hhu.service.strategy;

import kotlin.Pair;
import top.emilejones.hhu.entity.FileNode;
import top.emilejones.hhu.entity.TextNode;

import java.util.List;

public interface RecallStrategy {
    List<Pair<FileNode, TextNode>> exec(List<Pair<FileNode, TextNode>> rawData);
}
