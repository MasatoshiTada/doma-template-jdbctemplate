package com.example;

import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.template.SqlStatement;
import org.seasar.doma.template.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * 2-way SQLを利用した検索・追加・更新・削除を行うクラスです。
 *
 * @author Masatoshi Tada (@suke_masa)
 * @see JdbcTemplate
 * @see SqlTemplate
 */
public class TwoWayJdbcTemplate {

    private static final Logger logger = LoggerFactory.getLogger(TwoWayJdbcTemplate.class);
    
    private final JdbcTemplate jdbcTemplate;
    
    private final Dialect dialect;

    public TwoWayJdbcTemplate(JdbcTemplate jdbcTemplate, Dialect dialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.dialect = dialect;
    }

    /**
     * SELECT文で検索を行って、結果を1件返します。
     * TwoWayJdbcException以外に発生する例外は JdbcTemplate#queryForObject(String, RowMapper, Object...) と同じです。
     *
     * @param sqlOnClassPath SQLファイルのパス（クラスパス内のパスを指定）
     * @param resultType 戻り値の型
     * @param sqlParams SQLに指定するパラメータ値
     * @return 検索結果
     * @throws TwoWayJdbcException SQLファイルに関する異常があった場合
     * @see JdbcTemplate#queryForObject(String, RowMapper, Object...)
     */
    public <T> T queryForObject(String sqlOnClassPath, Class<T> resultType, SqlParam<?>... sqlParams) throws TwoWayJdbcException {
        SqlStatement sqlStatement = getSqlStatement(sqlOnClassPath, sqlParams);
        Object[] params = getParams(sqlStatement);
        String rawSql = sqlStatement.getRawSql();
        T result = jdbcTemplate.queryForObject(rawSql, new DataClassRowMapper<>(resultType), params);
        if (logger.isDebugEnabled()) {
            logger.debug("Executed SQL: {}", sqlStatement.getFormattedSql());
        }
        return result;
    }

    /**
     * SELECT文で検索を行って、結果を0件以上返します。
     * TwoWayJdbcException以外に発生する例外は JdbcTemplate#query(String, RowMapper, Object...) と同じです。
     *
     * @param sqlOnClassPath SQLファイルのパス（クラスパス内のパスを指定）
     * @param resultType 戻り値の型
     * @param sqlParams SQLに指定するパラメータ値
     * @return 検索結果
     * @throws TwoWayJdbcException SQLファイルに関する異常があった場合
     * @see JdbcTemplate#query(String, RowMapper, Object...)
     */
    public <T> List<T> query(String sqlOnClassPath, Class<T> resultType, SqlParam<?>... sqlParams) throws TwoWayJdbcException {
        SqlStatement sqlStatement = getSqlStatement(sqlOnClassPath, sqlParams);
        Object[] params = getParams(sqlStatement);
        String rawSql = sqlStatement.getRawSql();
        List<T> result = jdbcTemplate.query(rawSql, new DataClassRowMapper<>(resultType), params);
        if (logger.isDebugEnabled()) {
            logger.debug("Executed SQL: {}", sqlStatement.getFormattedSql());
        }
        return result;
    }

    /**
     * INSERT文・UPDATE文・DELETE文を実行します。
     * TwoWayJdbcException以外に発生する例外は JdbcTemplate#update(String, Object...) と同じです。
     *
     * @param sqlOnClassPath SQLファイルのパス（クラスパス内のパスを指定）
     * @param sqlParams SQLに指定するパラメータ値
     * @return 更新した行数
     * @throws TwoWayJdbcException SQLファイルに関する異常があった場合
     * @see JdbcTemplate#update(String, Object...)
     */
    public int update(String sqlOnClassPath, SqlParam<?>... sqlParams) {
        SqlStatement sqlStatement = getSqlStatement(sqlOnClassPath, sqlParams);
        Object[] params = getParams(sqlStatement);
        String rawSql = sqlStatement.getRawSql();
        int rows = jdbcTemplate.update(rawSql, params);
        if (logger.isDebugEnabled()) {
            logger.debug("Executed SQL: {}", sqlStatement.getFormattedSql());
        }
        return rows;
    }

    /**
     * INSERT文・UPDATE文・DELETE文を実行します。
     * TwoWayJdbcException以外に発生する例外は JdbcTemplate#update(PreparedStatementCreator, KeyHolder) と同じです。
     *
     * @param sqlOnClassPath SQLファイルのパス（クラスパス内のパスを指定）
     * @param sqlParams SQLに指定するパラメータ値
     * @return 更新した行数と生成された主キー値を保持する UpdateResult
     * @throws TwoWayJdbcException SQLファイルに関する異常があった場合
     * @see JdbcTemplate#update(PreparedStatementCreator, KeyHolder)
     */
    public UpdateResult updateAndGetKey(String sqlOnClassPath, String pkColumnName, SqlParam<?>... sqlParams) {
        SqlStatement sqlStatement = getSqlStatement(sqlOnClassPath, sqlParams);
        String rawSql = sqlStatement.getRawSql();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rows = jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(rawSql, new String[]{pkColumnName});
            for (int i = 0; i < sqlParams.length; i++) {
                SqlParam<?> sqlParam = sqlParams[i];
                statement.setObject(i + 1, sqlParam.value(), StatementCreatorUtils.javaTypeToSqlParameterType(sqlParam.valueType()));
            }
            return statement;
        }, keyHolder);
        if (logger.isDebugEnabled()) {
            logger.debug("Executed SQL: {}", sqlStatement.getFormattedSql());
        }
        Number key = keyHolder.getKey();
        return new UpdateResult(rows, key);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private SqlStatement getSqlStatement(String sqlOnClassPath, SqlParam<?>... sqlParams) {
        URL sqlFileUrl = this.getClass().getClassLoader().getResource(sqlOnClassPath);
        if (sqlFileUrl == null) {
            throw new TwoWayJdbcException(sqlOnClassPath + " not found");
        }
        try {
            Path sqlFile = Path.of(sqlFileUrl.toURI());
            String sql = Files.readString(sqlFile, StandardCharsets.UTF_8);
            SqlTemplate sqlTemplate = new SqlTemplate(sql, dialect);
            for (SqlParam sqlParam : sqlParams) {
                sqlTemplate = sqlTemplate.add(sqlParam.name(), sqlParam.valueType(), sqlParam.value());
            }
            SqlStatement sqlStatement = sqlTemplate.execute();
            return sqlStatement;
        } catch (URISyntaxException e) {
            throw new TwoWayJdbcException(e);
        } catch (IOException e) {
            throw new TwoWayJdbcException(e);
        }
    }

    private Object[] getParams(SqlStatement sqlStatement) {
        Object[] params = sqlStatement.getArguments()
                .stream()
                .map(arg -> arg.getValue())
                .toArray();
        return params;
    }
}
