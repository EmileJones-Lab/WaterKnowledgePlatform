package top.emilejones.hhu.mcp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.emilejones.huu.env.AutoFindConfigFile;
import top.emilejones.huu.env.pojo.ApplicationConfig;

@Configuration
public class BeanConfiguration {
    @Bean
    public ApplicationConfig getConfig(){
        return AutoFindConfigFile.INSTANCE.find();
    }
}
