package core.dataSource;

import core.em.Properties;

import javax.sql.DataSource;

public interface SimpleDataSource extends DataSource {

    void initDataSource(Properties property);

}
