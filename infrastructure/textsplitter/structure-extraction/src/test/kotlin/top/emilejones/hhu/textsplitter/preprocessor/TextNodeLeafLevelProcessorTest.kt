package top.emilejones.hhu.textsplitter.preprocessor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO

class TextNodeLeafLevelProcessorTest {

    @Test
    fun `should process children when all children are leaves`() {
        // Arrange
        val root = TextNodeDTO("root", "", 0, 0, TextType.NULL)
        val child1 = TextNodeDTO("c1", "## Child 1", 0, 1, TextType.COMMON_TEXT)
        val child2 = TextNodeDTO("c2", "Child 2", 1, 1, TextType.COMMON_TEXT)
        root.addChild(child1)
        root.addChild(child2)

        val processor = TextNodeLeafLevelProcessor(root)

        // Act
        processor.run()

        // Assert
        assertEquals(Int.MAX_VALUE, child1.level)
        assertEquals(TextType.TITLE, child1.type)
        assertEquals("Child 1", child1.text)

        assertEquals(Int.MAX_VALUE, child2.level)
        assertEquals(TextType.TITLE, child2.type)
        assertEquals("Child 2", child2.text)
    }

    @Test
    fun `should not process children when at least one child is not a leaf`() {
        // Arrange
        val root = TextNodeDTO("root", "", 0, 0, TextType.NULL)
        val child1 = TextNodeDTO("c1", "Child 1", 0, 1, TextType.COMMON_TEXT)
        val child2 = TextNodeDTO("c2", "Child 2", 0, 1, TextType.COMMON_TEXT)
        val grandChild = TextNodeDTO("gc1", "Grandchild", 0, 2, TextType.COMMON_TEXT)
        child1.addChild(grandChild)
        root.addChild(child1)
        root.addChild(child2)

        val processor = TextNodeLeafLevelProcessor(root)

        // Act
        processor.run()

        // Assert
        // root's children (child1) should NOT be processed because child1 has a child
        assertNotEquals(Int.MAX_VALUE, child1.level)
        assertEquals(1, child1.level)
        assertEquals(TextType.COMMON_TEXT, child1.type)

        // grandChild IS a leaf, but its parent (child1) only has leaf children (grandChild)
        // so grandChild SHOULD be processed
        assertEquals(Int.MAX_VALUE, grandChild.level)
        assertEquals(TextType.TITLE, grandChild.type)
    }

    @Test
    fun `should remove multiple leading hashes`() {
        // Arrange
        val root = TextNodeDTO("root", "", 0, 0, TextType.NULL)
        val child1 = TextNodeDTO("c1", "#### Heavy Header", 0, 1, TextType.TITLE)
        val child2 = TextNodeDTO("c2", " #### Heavy Header", 0, 1, TextType.TITLE)
        root.addChild(child1)
        root.addChild(child2)


        val processor = TextNodeLeafLevelProcessor(root)

        // Act
        processor.run()

        // Assert
        assertEquals("Heavy Header", child1.text)
        assertEquals("Heavy Header", child2.text)
    }

    @Test
    fun `should not change type if not COMMON_TEXT`() {
        // Arrange
        val root = TextNodeDTO("root", "", 0, 0, TextType.NULL)
        val child = TextNodeDTO("c1", "Image node", 0, 1, TextType.IMAGE)
        root.addChild(child)

        val processor = TextNodeLeafLevelProcessor(root)

        // Act
        processor.run()

        // Assert
        assertEquals(TextType.IMAGE, child.type)
        assertEquals(Int.MAX_VALUE, child.level)
    }

    @Test
    fun `should not process twice`() {
        // Arrange
        val root = TextNodeDTO("root", "", 0, 0, TextType.NULL)
        val child = TextNodeDTO("c1", "Child", 0, 1, TextType.COMMON_TEXT)
        root.addChild(child)

        val processor = TextNodeLeafLevelProcessor(root)

        // Act
        processor.run()
        assertEquals(Int.MAX_VALUE, child.level)
        
        // Change it back to simulate something else
        child.level = 5
        
        processor.run()
        
        // Should not have changed because isProcessed is true
        assertEquals(5, child.level)
    }

    @Test
    fun `should handle null or empty text`() {
        // Arrange
        val root = TextNodeDTO("root", "", 0, 0, TextType.NULL)
        val childEmpty = TextNodeDTO("c1", "", 0, 1, TextType.COMMON_TEXT)
        // TextNodeDTO text is not nullable based on its definition, but let's be safe if it's used with reflection or something
        // Actually TextNodeDTO says `var text: String`, so it's non-nullable in Kotlin.
        
        root.addChild(childEmpty)

        val processor = TextNodeLeafLevelProcessor(root)

        // Act
        processor.run()

        // Assert
        assertEquals("", childEmpty.text)
        assertEquals(Int.MAX_VALUE, childEmpty.level)
    }
}
