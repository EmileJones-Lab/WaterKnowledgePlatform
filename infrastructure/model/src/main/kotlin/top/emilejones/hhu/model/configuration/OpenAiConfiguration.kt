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
        val apiKey = modelConfig.llmToken
        val openAiApi = OpenAiApi.builder()
            .apiKey { apiKey }
            .baseUrl(modelConfig.llmUrl)
            .build()

        val options: OpenAiChatOptions? = OpenAiChatOptions.builder()
            .model(modelConfig.llmModel)
            .extraBody(mapOf("enable_thinking" to false))
            .temperature(0.1)
            .build()

        return OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(options).build()
    }
}