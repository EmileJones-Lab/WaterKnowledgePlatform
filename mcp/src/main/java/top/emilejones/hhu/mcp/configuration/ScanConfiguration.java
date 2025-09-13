package top.emilejones.hhu.mcp.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"top.emilejones.hhu.web.repository", "top.emilejones.hhu.web.service"})
public class ScanConfiguration {
}
