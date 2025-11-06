package top.emilejones.hhu.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.emilejones.hhu.milvus.MilvusRepository;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.model.impl.ModelClientByHttp;
import top.emilejones.hhu.neo4j.Neo4jRepository;
import top.emilejones.hhu.repository.IMilvusRepository;
import top.emilejones.hhu.repository.INeo4jRepository;
import top.emilejones.hhu.service.impl.RecallService;
import top.emilejones.huu.env.AutoFindConfigFile;
import top.emilejones.huu.env.pojo.ApplicationConfig;
import top.emilejones.huu.env.pojo.MilvusConfig;
import top.emilejones.huu.env.pojo.Neo4jConfig;

@Configuration
public class BeanConfiguration {
    @Bean
    public ApplicationConfig getConfig() {
        return AutoFindConfigFile.INSTANCE.find();
    }

    @Bean
    public ModelClient getModelClient(ApplicationConfig config) {
        return new ModelClientByHttp(
                config.getModel().getHost(),
                config.getModel().getPort(),
                config.getModel().getToken(),
                config.getModel().getEmbeddingModel(),
                config.getModel().getRerankModel()
        );
    }

    @Bean
    public RecallService getRecallService(ApplicationConfig config, IMilvusRepository milvusRepository, INeo4jRepository neo4jRepository, ModelClient modelClient) {
        return new RecallService(milvusRepository, neo4jRepository, modelClient, config.getRag().getRecallNumber());
    }

    @Bean
    public IMilvusRepository getMilvusRepository(ApplicationConfig config) {
        MilvusConfig milvusConfig = config.getMilvus();
        return new MilvusRepository(milvusConfig.getHost(), milvusConfig.getPort(), milvusConfig.getDatabase(), milvusConfig.getCollection());
    }

    @Bean
    public INeo4jRepository getNeo4jRepository(ApplicationConfig config) {
        Neo4jConfig neo4jConfig = config.getNeo4j();
        return new Neo4jRepository(neo4jConfig.getHost(), neo4jConfig.getPort(), neo4jConfig.getUser(), neo4jConfig.getPassword(), neo4jConfig.getDatabase());
    }
}
