package top.emilejones.hhu.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.model.impl.XinferenceHttpClient;
import top.emilejones.huu.env.AutoFindConfigFile;
import top.emilejones.huu.env.pojo.ApplicationConfig;

@Configuration
public class BeanConfiguration {
    @Bean
    public ApplicationConfig getConfig() {
        return AutoFindConfigFile.INSTANCE.find();
    }

    @Bean
    public ModelClient getModelClient(ApplicationConfig config) {
        return new XinferenceHttpClient(config.getXinference().getHost(), config.getXinference().getPort());
    }
}
