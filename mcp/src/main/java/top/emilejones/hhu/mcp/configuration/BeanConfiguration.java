package top.emilejones.hhu.mcp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.model.impl.ModelClientByHttp;
import top.emilejones.huu.env.AutoFindConfigFile;
import top.emilejones.huu.env.pojo.ApplicationConfig;

@Configuration
public class BeanConfiguration {
    @Bean
    public ApplicationConfig getConfig() {
        return AutoFindConfigFile.INSTANCE.find();
    }

    @Bean
    public ModelClient getClient(ApplicationConfig applicationConfig) {
        return new ModelClientByHttp(applicationConfig.getModel().getHost(),
                applicationConfig.getModel().getPort(),
                applicationConfig.getModel().getToken(),
                applicationConfig.getModel().getEmbeddingModel(),
                applicationConfig.getModel().getRerankModel());
    }
}
