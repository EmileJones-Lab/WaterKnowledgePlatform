package top.emilejones.hhu.textsplitter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.emilejones.hhu.preprocessing.structure.MarkdownStructureExtractor
import top.emilejones.hhu.preprocessing.structure.TitleTreeExtractor

@Configuration
class MarkdownStructureExtractorConfig {
    @Bean
    fun getMarkdownStructureExtractorConfig(): MarkdownStructureExtractor = TitleTreeExtractor()
}