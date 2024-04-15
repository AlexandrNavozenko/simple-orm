package core.query;

import core.AnnotationResolver;

public class SqlQueryBuilder {

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM %s WHERE %s = ?";

    private SqlQueryBuilder() {
        //Empty constructor
    }

    public static String getSelectQueryById(Class<?> clazz) {
        String tableName = AnnotationResolver.getTableName(clazz);
        String idColumnName = AnnotationResolver.getIdColumnName(clazz);

        return String.format(SELECT_BY_ID_SQL, tableName, idColumnName);
    }
}
