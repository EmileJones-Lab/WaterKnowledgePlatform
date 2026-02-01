package top.emilejones.hhu.web.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "文本切割和文本召回相关的接口文档",
                version = "1.0.0",
                description = "此接口文档是关于RAG系统中的相关功能的相关约束，例如数据预处理、文本召回等",
                contact = @Contact(
                        name = "EmileJones",
                        email = "021027shf@gmail.com"
                )
        ),
        servers = {
                @Server(url = "http://10.196.83.122:7456/", description = "实验室服务器开发环境"),
                @Server(url = "http://localhost:7456/", description = "本地测试环境")
        }
)
public class OpenApiConfiguration {
}
