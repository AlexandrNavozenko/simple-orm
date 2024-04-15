package core;

import core.annotation.Column;
import core.annotation.Id;
import core.annotation.Table;
import core.exception.AnnotationTableNameException;

import java.lang.reflect.Field;
import java.util.Arrays;

public class AnnotationResolver {

    private AnnotationResolver() {
        //Empty constructor
    }

    public static String getTableName(Class<?> clazz) {
        Table annotation = clazz.getAnnotation(Table.class);
        if (annotation == null) {
            throw new AnnotationTableNameException(String.format("Entity [%s] have not @Table annotation", clazz.getName()));
        }

        String name = annotation.name();

        return name.isEmpty() ? clazz.getSimpleName() : annotation.name();
    }

    public static String getColumnName(Field field) {
        Column annotation = field.getAnnotation(Column.class);

        return annotation == null || annotation.name().isEmpty() ? field.getName() : annotation.name();
    }

    public static String getIdColumnName(Class<?> clazz) {
        Field idField = getIdField(clazz);

        return getColumnName(idField);
    }

    private static Field getIdField(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny()
                .orElseThrow();
    }
}
