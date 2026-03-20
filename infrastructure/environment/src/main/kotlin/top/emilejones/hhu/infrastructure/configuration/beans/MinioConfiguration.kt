package top.emilejones.hhu.infrastructure.configuration.beans

import io.minio.MinioClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.emilejones.hhu.infrastructure.configuration.env.pojo.MinioConfig

@Configuration
class MinioConfiguration {

    @Bean
    fun minioClient(minioConfig: MinioConfig): MinioClient {
        val endpoint = "http://%s:%d".format(minioConfig.host, minioConfig.port)
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(minioConfig.user, minioConfig.password)
            .build()
    }
}
