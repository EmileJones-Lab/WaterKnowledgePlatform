package top.emilejones.hhu.service.strategy;

import kotlin.Pair;
import top.emilejones.hhu.domain.enums.TextType;
import top.emilejones.hhu.entity.FileNode;
import top.emilejones.hhu.entity.TextNode;
import top.emilejones.hhu.repository.INeo4jRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObtainWholeTableStrategy implements RecallStrategy {
    private final INeo4jRepository neo4jRepository;

    public ObtainWholeTableStrategy(INeo4jRepository neo4jRepository) {
        this.neo4jRepository = neo4jRepository;
    }

    @Override
    public List<Pair<FileNode, TextNode>> exec(List<Pair<FileNode, TextNode>> rawData) {
        Set<Pair<FileNode, TextNode>> resultSet = new HashSet<>();
        for (Pair<FileNode, TextNode> datum : rawData) {
            resultSet.add(datum);
            // 如果不是表格则跳过上下查找
            if (!TextType.TABLE.equals(datum.getSecond().getType()))
                continue;
            // 向下找
            Pair<FileNode, TextNode> nowPair = datum;
            while (TextType.TABLE.equals(nowPair.getSecond().getType())) {
                resultSet.add(nowPair);
                nowPair = neo4jRepository.nextNode(nowPair.getSecond().getElementId());
                if (resultSet.contains(nowPair))
                    break;
            }
            // 向上找
            nowPair = datum;
            while (TextType.TABLE.equals(nowPair.getSecond().getType())) {
                resultSet.add(nowPair);
                nowPair = neo4jRepository.preNode(nowPair.getSecond().getElementId());
                if (resultSet.contains(nowPair))
                    break;
            }
        }
        return resultSet.stream().toList();
    }
}
