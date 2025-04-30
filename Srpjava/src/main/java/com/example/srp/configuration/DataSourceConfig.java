package com.example.srp.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/srp?serverTimezone=Asia/Shanghai&useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("Wang0324");
        return dataSource;
    }
}

