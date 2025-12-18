package top.emilejones.hhu.pipeline.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.emilejones.hhu.env.pojo.MinioConfig;

@Configuration
public class MinioConnectConfig {

    @Bean
    public MinioClient minioClient(MinioConfig minioConfig) {
        String endpoint = String.format(
                "http://%s:%d",
                minioConfig.getHost(),
                minioConfig.getPort()
        );

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(minioConfig.getUser(), minioConfig.getPassword())
                .build();
    }
}
