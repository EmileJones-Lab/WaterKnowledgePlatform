package top.emilejones.hhu.model.configuration

import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.emilejones.hhu.infrastructure.configuration.env.pojo.OpenAiConfig


@Configuration
class OpenAiConfiguration {

    @Bean
    fun getOpenAiChatModel(modelConfig: OpenAiConfig): OpenAiChatModel {
        val token = modelConfig.llmToken
        val openAiApiBuilder = OpenAiApi.builder()
            .baseUrl(modelConfig.llmUrl)
        if (!token.isNullOrBlank())
            openAiApiBuilder.apiKey { token }
        val openAiApi = openAiApiBuilder.build()
        val options: OpenAiChatOptions? = OpenAiChatOptions.builder()
            .model(modelConfig.llmModel)
            .temperature(0.7)
            .build()

        return OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(options).build()
    }
}