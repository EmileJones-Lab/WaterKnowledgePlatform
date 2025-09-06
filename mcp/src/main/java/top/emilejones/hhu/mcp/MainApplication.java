package top.emilejones.hhu.mcp;


import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import top.emilejones.hhu.mcp.service.Neo4jService;
import top.emilejones.hhu.mcp.service.RecallService;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider neo4jTools(Neo4jService neo4jService) {
        return MethodToolCallbackProvider.builder().toolObjects(neo4jService).build();
    }
    @Bean
    public ToolCallbackProvider recallTools(RecallService recallService) {
        return MethodToolCallbackProvider.builder().toolObjects(recallService).build();
    }

}
