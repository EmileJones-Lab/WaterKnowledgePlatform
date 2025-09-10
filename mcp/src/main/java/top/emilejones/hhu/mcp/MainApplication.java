package top.emilejones.hhu.mcp;


import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import top.emilejones.hhu.mcp.mcp.Neo4jMcp;
import top.emilejones.hhu.mcp.mcp.RecallMcp;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider neo4jTools(Neo4jMcp neo4jMcp) {
        return MethodToolCallbackProvider.builder().toolObjects(neo4jMcp).build();
    }

    @Bean
    public ToolCallbackProvider recallTools(RecallMcp recallMcp) {
        return MethodToolCallbackProvider.builder().toolObjects(recallMcp).build();
    }

}
