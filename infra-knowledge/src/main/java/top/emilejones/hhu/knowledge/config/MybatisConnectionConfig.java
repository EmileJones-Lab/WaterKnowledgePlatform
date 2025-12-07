package top.emilejones.hhu.knowledge.config;

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
 * Configure MySQL datasource and MyBatis session factory using shared MysqlConfig.
 */
@Configuration
@MapperScan(
        basePackages = "top.emilejones.hhu.knowledge.mapper",
        sqlSessionFactoryRef = "knowledgeSqlSessionFactory",
        sqlSessionTemplateRef = "knowledgeSqlSessionTemplate"
)
public class MybatisConnectionConfig {

    @Bean(name = "knowledgeDataSource")
    public DataSource knowledgeDataSource(MysqlConfig mysqlConfig) {
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

    @Bean(name = "knowledgeSqlSessionFactory")
    public SqlSessionFactory knowledgeSqlSessionFactory(
            @Qualifier("knowledgeDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/*.xml"));
        // 注册自定义的 TypeHandler，确保枚举和数据库整数类型映射正确
        factoryBean.setTypeHandlersPackage("top.emilejones.hhu.knowledge.handler");
        return factoryBean.getObject();
    }

    @Bean(name = "knowledgeSqlSessionTemplate")
    public SqlSessionTemplate knowledgeSqlSessionTemplate(
            @Qualifier("knowledgeSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "knowledgeTransactionManager")
    public DataSourceTransactionManager knowledgeTransactionManager(
            @Qualifier("knowledgeDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
