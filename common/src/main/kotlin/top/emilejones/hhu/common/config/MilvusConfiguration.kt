package top.emilejones.hhu.common.config

import io.milvus.v2.client.ConnectConfig
import io.milvus.v2.client.MilvusClientV2
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.emilejones.hhu.common.env.pojo.MilvusConfig

@Configuration
class MilvusConfiguration {

    @Bean
    fun milvusClient(milvusConfig: MilvusConfig): MilvusClientV2 {
        return MilvusClientV2(
            ConnectConfig.builder()
                .uri("http://%s:%d".format(milvusConfig.host, milvusConfig.port))
                .build()
        )
    }
}
