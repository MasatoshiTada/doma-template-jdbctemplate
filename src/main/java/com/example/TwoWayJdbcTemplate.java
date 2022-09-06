package com.example;

import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.template.SqlArgument;
import org.seasar.doma.template.SqlStatement;
import org.seasar.doma.template.SqlTemplate;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TwoWayJdbcTemplate {
    
    private final JdbcTemplate jdbcTemplate;
    
    private final Dialect dialect;

    public TwoWayJdbcTemplate(DataSource dataSource, Dialect dialect) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dialect = dialect;
    }
    
    public <T> T queryForObject(String sqlOnClassPath, Class<T> resultType, SqlParam... params) throws TwoWayJdbcException {
        URL sqlFileUrl = this.getClass().getClassLoader().getResource(sqlOnClassPath);
        if (sqlFileUrl == null) {
            throw new TwoWayJdbcException(sqlOnClassPath + " not found");
        }
        try {
            Path sqlFile = Path.of(sqlFileUrl.toURI());
            String sql = Files.readString(sqlFile, StandardCharsets.UTF_8);
            SqlTemplate sqlTemplate = new SqlTemplate(sql, dialect);
            for (SqlParam param : params) {
                sqlTemplate = sqlTemplate.add(param.name(), param.valueType(), param.value());  // FIXME コンパイルエラー
            }
            SqlStatement sqlStatement = sqlTemplate.execute();
            List<SqlArgument> arguments = sqlStatement.getArguments();
            String rawSql = sqlStatement.getRawSql();
            return jdbcTemplate.queryForObject(rawSql, new DataClassRowMapper<>(resultType), arguments.toArray());
        } catch (URISyntaxException e) {
            throw new TwoWayJdbcException(e);
        } catch (IOException e) {
            throw new TwoWayJdbcException(e);
        }
    }
}
