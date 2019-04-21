package com.dinghz.tcpproxy.tcp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

/**
 * TcpApplication
 *
 * @author dinghz
 * @date 2019-04-20
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
@Configuration
@SpringBootApplication
public class TcpApplication {

    static {
        String logDir = System.getProperty("log.dir");
        if (logDir == null) {
            System.setProperty("log.dir", "./logs");
        }
    }

    @Bean(name = "dataSource", destroyMethod = "")
    @Qualifier("dataSource")
    public DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.sqlite.JDBC");
        dataSourceBuilder.url("jdbc:sqlite:" + "tcp-proxy.db");
        dataSourceBuilder.type(SQLiteDataSource.class);
        return dataSourceBuilder.build();
    }

    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean localEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource())
                .packages("com.dinghz.tcpproxy.tcp.domain")
                .persistenceUnit("local")
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(TcpApplication.class, args);
    }
}
