package top.emilejones.hhu.textsplitter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.preprocessing.structure.MarkdownStructureExtractor
import top.emilejones.hhu.preprocessing.structure.TitleTreeExtractor
import top.emilejones.hhu.preprocessing.structure.TitleTreeExtractorWithAI

@Configuration
class MarkdownStructureExtractorConfig {
    @Bean
    fun getMarkdownStructureExtractorConfig(modelClient: ModelClient): MarkdownStructureExtractor =
        TitleTreeExtractorWithAI(TitleTreeExtractor(), modelClient)
}