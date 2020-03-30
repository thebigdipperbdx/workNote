package com.sto.transport.event.infrastructure.util.driver;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;

import org.apache.ibatis.executor.loader.javassist.JavassistProxyFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
@MapperScan(basePackages = OracleConfig.PACKAGE, sqlSessionFactoryRef = "oracleSqlSessionFactory")
public class OracleConfig {
    private static Logger logger = LoggerFactory.getLogger(OracleConfig.class);

    @Value("${datasource.oracle.driverClassName}")
    private String driver;

    @Value("${datasource.oracle.url}")
    private String oracleUrl;

    @Value("${datasource.oracle.username}")
    private String oracleUsername;

    @Value("${datasource.oracle.password}")
    private String oraclePassword;

    static final String PACKAGE = "com.sto.transport.event.application.acl.**.repository.**";

    @Bean(name = "oracleDataSource")
    @Primary
    public DataSource oracleDataSource() {
        return getDataSource(driver, oracleUrl, oracleUsername, oraclePassword, logger);
    }

   public static DataSource getDataSource(String driver, String oracleUrl, String oracleUsername, String oraclePassword,
        Logger logger) {
        DruidDataSource dataSource = new DruidDataSource();

        dataSource.setDriverClassName(driver);
        dataSource.setUrl(oracleUrl);
        dataSource.setUsername(oracleUsername);
        dataSource.setPassword(oraclePassword);

        //dataSource.setValidationQuery("select sysdate from dual");

        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        return dataSource;
    }

    @Bean(name = "oracleTransactionManager")
    @Primary
    public DataSourceTransactionManager oracleTransactionManager() {
        return new DataSourceTransactionManager(oracleDataSource());
    }

    @Bean(name = "oracleSqlSessionFactory")
    @Primary
    public SqlSessionFactory oracleSqlSessionFactory(@Qualifier("oracleDataSource") DataSource oracleDataSource)
        throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(oracleDataSource);
        sessionFactory.setConfiguration(MybatisConfig.getMyBatisConfig());
        return sessionFactory.getObject();
    }



}
