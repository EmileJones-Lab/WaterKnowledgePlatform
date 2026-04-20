package top.emilejones.hhu.textsplitter.preprocessor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO

class LatexBlockMergeToolTest {

    @Test
    fun `should merge multi-line latex blocks`() {
        // Arrange
        val root = TextNodeDTO("root", "", -1, 0, TextType.NULL)
        val n1 = TextNodeDTO("n1", "Before", 0, 1, TextType.COMMON_TEXT)
        val n2 = TextNodeDTO("n2", "$$", 1, 1, TextType.COMMON_TEXT)
        val n3 = TextNodeDTO("n3", "a + b = c", 2, 1, TextType.COMMON_TEXT)
        val n4 = TextNodeDTO("n4", "$$", 3, 1, TextType.COMMON_TEXT)
        val n5 = TextNodeDTO("n5", "After", 4, 1, TextType.COMMON_TEXT)

        // 设置父子关系
        root.addChild(n1); n1.parentNode = root
        root.addChild(n2); n2.parentNode = root
        root.addChild(n3); n3.parentNode = root
        root.addChild(n4); n4.parentNode = root
        root.addChild(n5); n5.parentNode = root

        // 设置序列关系 (preNode/nextNode)
        root.nextNode = n1; n1.preNode = root
        n1.nextNode = n2; n2.preNode = n1
        n2.nextNode = n3; n3.preNode = n2
        n3.nextNode = n4; n4.preNode = n3
        n4.nextNode = n5; n5.preNode = n4

        val tool = LatexBlockMergeTool(root)

        // Act
        tool.run()

        // Assert
        // 验证结构: root -> n1 -> merged -> n5
        assertEquals(3, root.childNum())
        assertEquals("Before", root.getChild(0).text)
        assertEquals("$$\na + b = c\n$$", root.getChild(1).text)
        assertEquals(TextType.LATEX, root.getChild(1).type)
        assertEquals("After", root.getChild(2).text)

        val merged = root.getChild(1)
        assertEquals(root.getChild(0), merged.preNode)
        assertEquals(root.getChild(2), merged.nextNode)
        assertEquals(merged, root.getChild(0).nextNode)
        assertEquals(merged, root.getChild(2).preNode)

        // 验证 seq 是否纠正
        assertEquals(0, root.getChild(0).seq)
        assertEquals(1, root.getChild(1).seq)
        assertEquals(2, root.getChild(2).seq)
    }

    @Test
    fun `should handle multiple latex blocks`() {
        // Arrange
        val root = TextNodeDTO("root", "", -1, 0, TextType.NULL)
        val n1 = TextNodeDTO("n1", "$$", 0, 1, TextType.COMMON_TEXT)
        val n2 = TextNodeDTO("n2", "block1", 1, 1, TextType.COMMON_TEXT)
        val n3 = TextNodeDTO("n3", "$$", 2, 1, TextType.COMMON_TEXT)
        val n4 = TextNodeDTO("n4", "between", 3, 1, TextType.COMMON_TEXT)
        val n5 = TextNodeDTO("n5", "$$", 4, 1, TextType.COMMON_TEXT)
        val n6 = TextNodeDTO("n6", "block2", 5, 1, TextType.COMMON_TEXT)
        val n7 = TextNodeDTO("n7", "$$", 6, 1, TextType.COMMON_TEXT)

        val nodes = listOf(n1, n2, n3, n4, n5, n6, n7)
        var pre: TextNodeDTO = root
        for (node in nodes) {
            root.addChild(node)
            node.parentNode = root
            pre.nextNode = node
            node.preNode = pre
            pre = node
        }

        val tool = LatexBlockMergeTool(root)

        // Act
        tool.run()

        // Assert
        assertEquals(3, root.childNum())
        assertEquals("$$\nblock1\n$$", root.getChild(0).text)
        assertEquals(TextType.LATEX, root.getChild(0).type)
        assertEquals("between", root.getChild(1).text)
        assertEquals("$$\nblock2\n$$", root.getChild(2).text)
        assertEquals(TextType.LATEX, root.getChild(2).type)
        
        assertEquals(0, root.getChild(0).seq)
        assertEquals(1, root.getChild(1).seq)
        assertEquals(2, root.getChild(2).seq)
    }

    @Test
    fun `should not merge if end delimiter is missing`() {
        // Arrange
        val root = TextNodeDTO("root", "", -1, 0, TextType.NULL)
        val n1 = TextNodeDTO("n1", "$$", 0, 1, TextType.COMMON_TEXT)
        val n2 = TextNodeDTO("n2", "no end", 1, 1, TextType.COMMON_TEXT)

        root.addChild(n1); n1.parentNode = root
        root.addChild(n2); n2.parentNode = root
        root.nextNode = n1; n1.preNode = root
        n1.nextNode = n2; n2.preNode = n1

        val tool = LatexBlockMergeTool(root)

        // Act
        tool.run()

        // Assert
        assertEquals(2, root.childNum())
        assertEquals("$$", root.getChild(0).text)
        assertEquals("no end", root.getChild(1).text)
    }
}
