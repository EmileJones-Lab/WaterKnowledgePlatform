package top.emilejones.hhu.model.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class LlmExecutorConfiguration {

    @Bean
    public ExecutorService llmExecutorService() {
        return Executors.newCachedThreadPool();
    }
}
