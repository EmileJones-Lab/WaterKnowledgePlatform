package top.emilejones.hhu.pipeline.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import top.emilejones.hhu.env.pojo.MysqlConfig;

import javax.sql.DataSource;

/**
 * MyBatis wiring that binds mapper scanning to the connection beans provided by MysqlConnectConfig.
 */
@Configuration
@MapperScan(
        basePackages = "top.emilejones.hhu.pipeline.mapper",
        sqlSessionFactoryRef = "infra-pipeline-SqlSessionFactory"
)
public class MybatisConnectConfig {

    @Bean(name = "infra-pipeline-DataSource")
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

    @Bean(name = "infra-pipeline-SqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("infra-pipeline-DataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml")
        );
        return factory.getObject();
    }

    @Bean(name = "infra-pipeline-SqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("infra-pipeline-SqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "pipelineTransactionManager")
    public DataSourceTransactionManager documentTransactionManager(@Qualifier("infra-pipeline-DataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
