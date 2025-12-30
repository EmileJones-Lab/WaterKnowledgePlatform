package top.emilejones.hhu.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.emilejones.hhu.domain.knowledge.service.KnowledgeDomainService;

@Configuration
public class DomainServiceConfig {
    @Bean
    public KnowledgeDomainService getKnowledgeDomainService(){
        return new KnowledgeDomainService();
    }
}
