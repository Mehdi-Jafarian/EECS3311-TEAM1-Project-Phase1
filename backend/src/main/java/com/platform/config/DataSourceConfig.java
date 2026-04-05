package com.platform.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Configuration
    @ConditionalOnProperty(name = "DB_HOST")
    static class PostgresConfig {
        @Bean
        public DataSource dataSource(
                @org.springframework.beans.factory.annotation.Value("${DB_HOST}") String host,
                @org.springframework.beans.factory.annotation.Value("${DB_PORT:5432}") String port,
                @org.springframework.beans.factory.annotation.Value("${DB_NAME:booking_platform}") String dbName,
                @org.springframework.beans.factory.annotation.Value("${DB_USER:platform_user}") String user,
                @org.springframework.beans.factory.annotation.Value("${DB_PASSWORD:defaultpassword}") String password) {
            var ds = new org.springframework.jdbc.datasource.DriverManagerDataSource();
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setUrl("jdbc:postgresql://" + host + ":" + port + "/" + dbName);
            ds.setUsername(user);
            ds.setPassword(password);
            return ds;
        }

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }
}
