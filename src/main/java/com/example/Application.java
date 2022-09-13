package com.example;

import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.PostgresDialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Dialect dialect() {
        return new PostgresDialect();
    }

    @Bean
    public TwoWayJdbcTemplate twoWayJdbcTemplate(JdbcTemplate jdbcTemplate, Dialect dialect) {
        return new TwoWayJdbcTemplate(jdbcTemplate, dialect);
    }
}
