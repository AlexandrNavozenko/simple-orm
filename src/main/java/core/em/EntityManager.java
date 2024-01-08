package core.em;

import javax.sql.DataSource;

public interface EntityManager {

    <T> T find(Class<T> clazz, Long id);

    DataSource initDataSource(Properties property);
}
