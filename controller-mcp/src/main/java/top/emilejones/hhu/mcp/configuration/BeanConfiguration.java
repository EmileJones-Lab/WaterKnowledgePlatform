package top.emilejones.hhu.mcp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.model.impl.ModelClientByHttp;
import top.emilejones.hhu.repository.impl.neo4j.Neo4jRepositoryImpl;
import top.emilejones.hhu.repository.INeo4jRepository;
import top.emilejones.huu.env.AutoFindConfigFile;
import top.emilejones.huu.env.pojo.HttpModelClientConfig;
import top.emilejones.huu.env.pojo.Neo4jConfig;

@Configuration
@Import(AutoFindConfigFile.class)
public class BeanConfiguration {
    @Bean
    public ModelClient getClient(HttpModelClientConfig modelConfig) {
        return new ModelClientByHttp(modelConfig.getHost(),
                modelConfig.getPort(),
                modelConfig.getToken(),
                modelConfig.getEmbeddingModel(),
                modelConfig.getRerankModel());
    }

    @Bean
    public INeo4jRepository getNeo4jRepository(Neo4jConfig neo4jConfig) {
        return new Neo4jRepositoryImpl(neo4jConfig.getHost(), neo4jConfig.getPort(), neo4jConfig.getUser(), neo4jConfig.getPassword(), neo4jConfig.getDatabase());
    }
}
