package core.em;

import org.postgresql.ds.PGSimpleDataSource;

public class PostgresEntityManager extends DefaultEntityManager {

    public PostgresEntityManager(Properties property) {
        super(property);
    }

    public PostgresEntityManager(Properties property, int poolSize) {
        super(property, poolSize);
    }

    @Override
    public void initDataSource(Properties property) {
        PGSimpleDataSource simpleDataSource = new PGSimpleDataSource();
        simpleDataSource.setURL(property.url());
        simpleDataSource.setUser(property.user());
        simpleDataSource.setPassword(property.pass());

        setDataSource(simpleDataSource);
    }
}
