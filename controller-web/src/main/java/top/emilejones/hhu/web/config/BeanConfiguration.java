package top.emilejones.hhu.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.emilejones.hhu.milvus.SingleCollectionMilvusRepository;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.model.impl.ModelClientByHttp;
import top.emilejones.hhu.repository.impl.neo4j.Neo4jRepositoryImpl;
import top.emilejones.hhu.repository.IMilvusRepository;
import top.emilejones.hhu.repository.INeo4jRepository;
import top.emilejones.hhu.service.impl.RecallService;
import top.emilejones.huu.env.AutoFindConfigFile;
import top.emilejones.huu.env.pojo.HttpModelClientConfig;
import top.emilejones.huu.env.pojo.MilvusConfig;
import top.emilejones.huu.env.pojo.Neo4jConfig;
import top.emilejones.huu.env.pojo.RAGConfig;

@Configuration
@Import(AutoFindConfigFile.class)
public class BeanConfiguration {
    @Bean
    public ModelClient getModelClient(HttpModelClientConfig modelConfig) {
        return new ModelClientByHttp(
                modelConfig.getHost(),
                modelConfig.getPort(),
                modelConfig.getToken(),
                modelConfig.getEmbeddingModel(),
                modelConfig.getRerankModel()
        );
    }

    @Bean
    public RecallService getRecallService(RAGConfig ragConfig, IMilvusRepository milvusRepository, INeo4jRepository neo4jRepository, ModelClient modelClient) {
        return new RecallService(milvusRepository, neo4jRepository, modelClient, ragConfig.getRecallNumber());
    }

    @Bean
    public IMilvusRepository getMilvusRepository(MilvusConfig milvusConfig, HttpModelClientConfig modelConfig) {
        return new SingleCollectionMilvusRepository(milvusConfig.getHost(),
                milvusConfig.getPort(),
                milvusConfig.getDatabase(),
                milvusConfig.getCollection(),
                modelConfig.getDimension());
    }

    @Bean
    public INeo4jRepository getNeo4jRepository(Neo4jConfig neo4jConfig) {
        return new Neo4jRepositoryImpl(neo4jConfig.getHost(), neo4jConfig.getPort(), neo4jConfig.getUser(), neo4jConfig.getPassword(), neo4jConfig.getDatabase());
    }
}
