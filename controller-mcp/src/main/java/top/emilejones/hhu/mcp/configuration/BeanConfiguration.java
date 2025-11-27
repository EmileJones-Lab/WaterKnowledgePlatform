package top.emilejones.hhu.mcp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.model.impl.ModelClientByHttp;
import top.emilejones.hhu.repository.impl.neo4j.Neo4jRepositoryImpl;
import top.emilejones.hhu.repository.INeo4jRepository;
import top.emilejones.huu.env.AutoFindConfigFile;
import top.emilejones.huu.env.pojo.ApplicationConfig;
import top.emilejones.huu.env.pojo.Neo4jConfig;

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

    @Bean
    public INeo4jRepository getNeo4jRepository(ApplicationConfig config) {
        Neo4jConfig neo4jConfig = config.getNeo4j();
        return new Neo4jRepositoryImpl(neo4jConfig.getHost(), neo4jConfig.getPort(), neo4jConfig.getUser(), neo4jConfig.getPassword(), neo4jConfig.getDatabase());
    }
}
