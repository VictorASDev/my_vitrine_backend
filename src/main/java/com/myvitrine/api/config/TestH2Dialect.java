package com.myvitrine.api.config;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

public class TestH2Dialect extends H2Dialect {

    @Override
    public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
        return (sqlException, message, sql) -> {
            if ("23".equals(sqlException.getSQLState().substring(0, 2))) {
                throw new DataIntegrityViolationException(message, sqlException);
            }
            return null;
        };
    }
}