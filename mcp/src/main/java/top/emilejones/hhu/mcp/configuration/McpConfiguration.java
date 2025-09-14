package top.emilejones.hhu.mcp.configuration;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.emilejones.hhu.mcp.mcp.Neo4jMcp;
import top.emilejones.hhu.mcp.mcp.RecallMcp;

@Configuration
public class McpConfiguration {
    @Bean
    public ToolCallbackProvider neo4jTools(Neo4jMcp neo4jMcp) {
        return MethodToolCallbackProvider.builder().toolObjects(neo4jMcp).build();
    }

    @Bean
    public ToolCallbackProvider recallTools(RecallMcp recallMcp) {
        return MethodToolCallbackProvider.builder().toolObjects(recallMcp).build();
    }
}
