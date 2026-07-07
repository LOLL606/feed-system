package com.niit.feed.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.master.url}")
    private String masterUrl;

    @Value("${spring.datasource.master.username}")
    private String masterUsername;

    @Value("${spring.datasource.master.password}")
    private String masterPassword;

    @Value("${spring.datasource.slave.url:}")
    private String slaveUrl;

    @Value("${spring.datasource.slave.username:}")
    private String slaveUsername;

    @Value("${spring.datasource.slave.password:}")
    private String slavePassword;

    @Bean
    public DataSource masterDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(masterUrl);
        ds.setUsername(masterUsername);
        ds.setPassword(masterPassword);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setPoolName("master-pool");
        ds.setMaximumPoolSize(20);
        ds.setMinimumIdle(5);
        return ds;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.slave", name = "enabled", havingValue = "true")
    public DataSource slaveDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(slaveUrl);
        ds.setUsername(slaveUsername);
        ds.setPassword(slavePassword);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setPoolName("slave-pool");
        ds.setReadOnly(true);
        ds.setMaximumPoolSize(20);
        ds.setMinimumIdle(5);
        return ds;
    }

    @Bean
    @Primary
    public DataSource routingDataSource(DataSource masterDataSource,
                                         @org.springframework.beans.factory.annotation.Autowired(required = false) DataSource slaveDataSource) {
        RoutingDataSource routing = new RoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", masterDataSource);
        if (slaveDataSource != null) {
            targetDataSources.put("slave", slaveDataSource);
        }
        routing.setTargetDataSources(targetDataSources);
        routing.setDefaultTargetDataSource(masterDataSource);
        return routing;
    }
}
