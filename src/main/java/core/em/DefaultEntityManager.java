package core.em;

import core.ResultSetMapper;
import core.dataSource.PoolDataSource;
import core.exception.CreateNewInstanceException;
import core.exception.ExecuteQueryException;
import core.query.SqlQueryBuilder;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DefaultEntityManager extends PoolDataSource implements EntityManager {

    @Getter
    @Setter
    private DataSource dataSource;

    private final Map<EntityKey<?>, Object> cache = new ConcurrentHashMap<>();

    protected DefaultEntityManager(Properties property) {
        super(property);
    }

    protected DefaultEntityManager(Properties property, int poolSize) {
        super(property, poolSize);
    }

    @Override
    public abstract void initDataSource(Properties property);

    @Override
    protected Connection getRealConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public final <T> T find(Class<T> clazz, Long id) {
        EntityKey<T> entityKey = new EntityKey<>(clazz, id);

        return clazz.cast(cache.computeIfAbsent(entityKey, this::find));
    }

    private <T> T find(EntityKey<T> entityKey) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SqlQueryBuilder.getSelectQueryById(entityKey.clazz()))) {
                preparedStatement.setObject(1, entityKey.id());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return populateEntity(entityKey.clazz(), resultSet);
                }
            }

            return null;
        } catch (SQLException exception) {
            throw new ExecuteQueryException(String
                    .format("Fail executeQuery by Entity [%s]", entityKey.clazz().getSimpleName()), exception);
        }
    }

    private <T> T populateEntity(Class<T> clazz, ResultSet resultSet) {
        try {
            T entity = clazz.getConstructor().newInstance();
            ResultSetMapper.parse(entity, resultSet);

            return entity;
        } catch (Exception exception) {
            throw new CreateNewInstanceException("Fail create new instance from default constructor", exception);
        }
    }
}
