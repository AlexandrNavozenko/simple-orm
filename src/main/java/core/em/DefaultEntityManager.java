package core.em;

import core.ResultSetMapper;
import core.exception.CreateNewInstanceException;
import core.exception.ExecuteQueryException;
import core.query.SqlQueryBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DefaultEntityManager implements EntityManager {

    private final DataSource dataSource;

    protected DefaultEntityManager(Properties property) {
        this.dataSource = initDataSource(property);
    }

    public final <T> T find(Class<T> clazz, Long id) {
        try (Connection connection = dataSource.getConnection()) {
            EntityKey entityKey = new EntityKey(clazz, id);
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SqlQueryBuilder.getSelectQueryById(entityKey.clazz()))) {
                preparedStatement.setObject(1, entityKey.id());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return populateEntity(clazz, resultSet);
                }
            }

            return null;
        } catch (SQLException exception) {
            throw new ExecuteQueryException(String
                    .format("Fail executeQuery by Entity [%s]", clazz.getSimpleName()), exception);
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


    @Override
    public abstract DataSource initDataSource(Properties property);
}
