package top.emilejones.hhu.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author EmileJones
 */
@SpringBootApplication(scanBasePackages = {"top.emilejones.hhu"})
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
