package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TwoWayJdbcTemplateTest {

    @Autowired
    DataSource dataSource;
    
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TwoWayJdbcTemplate twoWayJdbcTemplate;

    private static final int SEQ_INIT_VALUE = 100;

    @BeforeEach
    public void beforeEach() {
        jdbcTemplate.queryForObject("SELECT setval('seq_employee_id', ?, false)", Long.class, SEQ_INIT_VALUE);
    }

    @Nested
    @DisplayName("SELECT")
    class SelectTest {
        @Test
        @DisplayName("IDでの単一検索")
        public void test01() {
            Employee actual = twoWayJdbcTemplate.queryForObject("com/example/selectEmployeeById.sql",
                    Employee.class,
                    new SqlParam<>("id", Integer.class, 2));
            Employee expected = new Employee(2, "Bob");
            assertEquals(expected, actual);
        }
    }

    @Nested
    @DisplayName("UPDATE")
    class UpdateTest {
        @Test
        @DisplayName("NULLをバインド")
        public void test01() {
            int actual = twoWayJdbcTemplate.update("com/example/updateEmployeeById.sql",
                    new SqlParam<>("name", String.class, null),
                    new SqlParam<>("joined_date", LocalDate.class, null),
                    new SqlParam<>("id", Integer.class, 2)
            );
            assertEquals(1, actual);
        }
    }

    @Nested
    @DisplayName("INSERT")
    class InsertTest {
        @Test
        @DisplayName("自動生成キーを取得")
        public void test01() {
            UpdateResult updateResult = twoWayJdbcTemplate.updateAndGetKey("com/example/insertEmployee.sql",
                    "id",
                    new SqlParam<>("name", String.class, "John Doe"),
                    new SqlParam<>("joined_date", LocalDate.class, LocalDate.of(2022, 12, 31))
            );
            assertAll(
                    () -> assertEquals(1, updateResult.updatedRows()),
                    () -> assertEquals(SEQ_INIT_VALUE, updateResult.key().intValue())
            );
        }

        @Test
        @DisplayName("NULLをバインド、自動生成キーを取得")
        public void test02() {
            UpdateResult updateResult = twoWayJdbcTemplate.updateAndGetKey("com/example/insertEmployee.sql",
                    "id",
                    new SqlParam<>("name", String.class, null),
                    new SqlParam<>("joined_date", LocalDate.class, null)
            );
            assertAll(
                    () -> assertEquals(1, updateResult.updatedRows()),
                    () -> assertEquals(SEQ_INIT_VALUE, updateResult.key().intValue())
            );
        }
    }
}
