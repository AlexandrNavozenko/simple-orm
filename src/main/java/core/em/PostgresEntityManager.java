package core.em;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class PostgresEntityManager extends DefaultEntityManager {
    public PostgresEntityManager(Properties property) {
        super(property);
    }

    @Override
    public DataSource initDataSource(Properties property) {
        PGSimpleDataSource simpleDataSource = new PGSimpleDataSource();
        simpleDataSource.setURL(property.url());
        simpleDataSource.setUser(property.user());
        simpleDataSource.setPassword(property.pass());

        return simpleDataSource;
    }
}
