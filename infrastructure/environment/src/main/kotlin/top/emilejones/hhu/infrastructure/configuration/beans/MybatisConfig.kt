package top.emilejones.hhu.infrastructure.configuration.beans

import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.SqlSessionTemplate
import org.mybatis.spring.annotation.MapperScan
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import top.emilejones.hhu.infrastructure.configuration.env.pojo.MysqlConfig
import javax.sql.DataSource

@Configuration
@MapperScan(basePackages = ["top.emilejones.hhu.**.mapper"])
class MybatisConfig {

    @Bean
    @Primary
    fun dataSource(mysqlConfig: MysqlConfig): DataSource {
        val url = "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true".format(
            mysqlConfig.host,
            mysqlConfig.port,
            mysqlConfig.database
        )
        return DataSourceBuilder.create()
            .driverClassName("com.mysql.cj.jdbc.Driver")
            .url(url)
            .username(mysqlConfig.user)
            .password(mysqlConfig.password)
            .build()
    }

    @Bean
    @Primary
    fun sqlSessionFactory(@Qualifier("dataSource") dataSource: DataSource): SqlSessionFactory {
        val factoryBean = SqlSessionFactoryBean()
        factoryBean.setDataSource(dataSource)
        factoryBean.setMapperLocations(
            *PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml")
        )
        // Set type handlers package to a common root or specific packages if known
        // Assuming scanning from root package recursively for type handlers
        factoryBean.setTypeHandlersPackage("top.emilejones.hhu.knowledge.handler,top.emilejones.hhu.common.handler") 
        return factoryBean.`object`!!
    }

    @Bean
    @Primary
    fun sqlSessionTemplate(@Qualifier("sqlSessionFactory") sqlSessionFactory: SqlSessionFactory): SqlSessionTemplate {
        return SqlSessionTemplate(sqlSessionFactory)
    }

    @Bean
    @Primary
    fun transactionManager(@Qualifier("dataSource") dataSource: DataSource): DataSourceTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }
}
