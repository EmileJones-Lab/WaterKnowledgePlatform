package top.emilejones.hhu.document.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import top.emilejones.hhu.env.pojo.MysqlConfig;

import javax.sql.DataSource;

/**
 * MyBatis wiring that binds mapper scanning to the connection beans provided by MysqlConnectionConfig.
 */
@Configuration
@MapperScan(
        basePackages = "top.emilejones.hhu.document.mapper",
        sqlSessionFactoryRef = "infra-document-SqlSessionFactory"
)
public class MybatisConfig {

    @Bean(name = "infra-document-DataSource")
    public DataSource dataSource(MysqlConfig mysqlConfig) {
        String url = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&characterEncoding=utf8&serverTimezone=UTC",
                mysqlConfig.getHost(),
                mysqlConfig.getPort(),
                mysqlConfig.getDatabase()
        );
        return DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url(url)
                .username(mysqlConfig.getUser())
                .password(mysqlConfig.getPassword())
                .build();
    }

    @Bean(name = "infra-document-SqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml")
        );
        return factory.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("infra-document-SqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
