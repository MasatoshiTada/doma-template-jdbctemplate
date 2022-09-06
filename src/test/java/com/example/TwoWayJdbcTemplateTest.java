package com.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.seasar.doma.jdbc.dialect.PostgresDialect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

public class TwoWayJdbcTemplateTest {
    
    static DataSource dataSource;
    
    static TwoWayJdbcTemplate twoWayJdbcTemplate;
    
    static JdbcTemplate jdbcTemplate;
    
    @BeforeAll
    public static void beforeAll() {
        dataSource = new DriverManagerDataSource("jdbc:postgresql://localhost:5444/twoway", "twoway", "twoway");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                DROP TABLE IF EXISTS employee;
                
                CREATE TABLE employee(
                    id INTEGER PRIMARY KEY,
                    name VARCHAR(32) NOT NULL
                );
                
                INSERT INTO employee(id, name) VALUES(1, 'Alice');
                INSERT INTO employee(id, name) VALUES(2, 'Bob');
                INSERT INTO employee(id, name) VALUES(3, 'Chris');
                """);
        twoWayJdbcTemplate = new TwoWayJdbcTemplate(dataSource, new PostgresDialect());
    }
    
    @Test
    @DisplayName("queryForObjectでID検索ができる")
    public void test01() {
        Employee actual = twoWayJdbcTemplate.queryForObject("com/example/selectEmpById.sql", Employee.class, new SqlParam<>("id", Integer.class, 2));
        Employee expected = new Employee(2, "Bob");
        assertEquals(expected, actual);
    }
}
