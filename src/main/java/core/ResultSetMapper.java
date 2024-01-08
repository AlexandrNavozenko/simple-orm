package core;

import core.exception.ParseResultSetException;

import java.lang.reflect.Field;
import java.sql.ResultSet;

public class ResultSetMapper {

    private ResultSetMapper() {
        //Empty constructor
    }

    public static void parse(Object entity, ResultSet resultSet) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            String columnName = AnnotationResolver.getColumnName(field);
            field.setAccessible(true);
            try {
                field.set(entity, resultSet.getObject(columnName));
            } catch (Exception exception) {
                throw new ParseResultSetException("Not parse ResultSet", exception);
            }
        }
    }
}
