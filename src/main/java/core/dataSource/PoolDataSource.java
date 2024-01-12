package core.dataSource;

import core.em.Properties;
import core.exception.ConnectionClearException;
import core.exception.InitPoolConnectionException;
import core.exception.NotFoundConnectionException;
import java.io.PrintWriter;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.ShardingKey;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class PoolDataSource implements SimpleDataSource {

    private static final int DEFAULT_POOL_SIZE = 10;

    private final Properties property;

    private final BlockingQueue<Connection> poolConnection;

    public PoolDataSource(Properties property) {
        this.poolConnection = new ArrayBlockingQueue<>(DEFAULT_POOL_SIZE);
        this.property = property;

        initDataSource(property);
        initPoolConnection(DEFAULT_POOL_SIZE);
    }

    public PoolDataSource(Properties property, int poolSize) {
        this.poolConnection = new ArrayBlockingQueue<>(poolSize);
        this.property = property;

        initDataSource(property);
        initPoolConnection(poolSize);
    }

    private void initPoolConnection(int poolSize) {
        for (int i = 0; i < poolSize; i++) {
            try {
                Connection connection = getRealConnection();
                poolConnection.add(new PoolConnection(connection, poolConnection));
            } catch (SQLException exception) {
                throw new InitPoolConnectionException("Pool connections not initialized", exception);
            }
        }
    }

    @Override
    public abstract void initDataSource(Properties property);

    protected Connection getRealConnection() throws SQLException {
        return DriverManager.getConnection(property.url(), property.user(), property.pass());
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return poolConnection.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            throw new NotFoundConnectionException("Exception not found connection", exception);
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        if (property.user().equals(username) && property.pass().equals(password)) {
            return getConnection();
        }

        throw new NotFoundConnectionException("Exception not found connection");
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Method not supported yet.");
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("Method not supported yet.");
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Method not supported yet.");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Method not supported yet.");
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("Method not supported yet.");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Method not supported yet.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Method not supported yet.");
    }

    static class PoolConnection implements Connection {

        private final Connection realConnection;

        private final BlockingQueue<Connection> poolConnection;

        public final List<Statement> openStatements = new ArrayList<>();

        PoolConnection(Connection realConnection, BlockingQueue<Connection> poolConnection) {
            this.realConnection = realConnection;
            this.poolConnection = poolConnection;
        }

        @Override
        public void close() throws SQLException {
            realConnection.getTypeMap().clear();
            realConnection.setAutoCommit(true);
            realConnection.setReadOnly(false);
            realConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            openStatements.forEach(statement -> {
                try {
                    statement.close();
                } catch (SQLException exception) {
                    throw new ConnectionClearException("Exception when cleaning open statements", exception);
                }
            });

            openStatements.clear();
            poolConnection.add(this);
        }

        @Override
        public Statement createStatement() throws SQLException {
            Statement statement = realConnection.createStatement();
            openStatements.add(statement);

            return statement;
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            PreparedStatement preparedStatement = realConnection.prepareStatement(sql);
            openStatements.add(preparedStatement);

            return preparedStatement;
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            CallableStatement callableStatement = realConnection.prepareCall(sql);
            openStatements.add(callableStatement);

            return callableStatement;
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return realConnection.nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            realConnection.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return realConnection.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            realConnection.commit();
        }

        @Override
        public void rollback() throws SQLException {
            realConnection.rollback();
        }

        @Override
        public boolean isClosed() throws SQLException {
            return realConnection.isClosed();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return realConnection.getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            realConnection.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return realConnection.isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            realConnection.setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            return realConnection.getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            realConnection.setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return realConnection.getTransactionIsolation();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return realConnection.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            realConnection.clearWarnings();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return realConnection.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return realConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return realConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return realConnection.getTypeMap();
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            realConnection.setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            realConnection.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return realConnection.getHoldability();
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return realConnection.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return realConnection.setSavepoint(name);
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            realConnection.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            realConnection.releaseSavepoint(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return realConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return realConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return realConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return realConnection.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return realConnection.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return realConnection.prepareStatement(sql, columnNames);
        }

        @Override
        public Clob createClob() throws SQLException {
            return realConnection.createClob();
        }

        @Override
        public Blob createBlob() throws SQLException {
            return realConnection.createBlob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            return realConnection.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return realConnection.createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return realConnection.isValid(timeout);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            realConnection.setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(java.util.Properties properties) throws SQLClientInfoException {
            realConnection.setClientInfo(properties);
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return realConnection.getClientInfo(name);
        }

        @Override
        public java.util.Properties getClientInfo() throws SQLException {
            return realConnection.getClientInfo();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return realConnection.createArrayOf(typeName, elements);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return realConnection.createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
            realConnection.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
            return realConnection.getSchema();
        }

        @Override
        public void abort(Executor executor) throws SQLException {
            realConnection.abort(executor);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            realConnection.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return realConnection.getNetworkTimeout();
        }

        @Override
        public void beginRequest() throws SQLException {
            realConnection.beginRequest();
        }

        @Override
        public void endRequest() throws SQLException {
            realConnection.endRequest();
        }

        @Override
        public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey, int timeout) throws SQLException {
            return realConnection.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
        }

        @Override
        public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
            return realConnection.setShardingKeyIfValid(shardingKey, timeout);
        }

        @Override
        public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey) throws SQLException {
            realConnection.setShardingKey(shardingKey, superShardingKey);
        }

        @Override
        public void setShardingKey(ShardingKey shardingKey) throws SQLException {
            realConnection.setShardingKey(shardingKey);
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return realConnection.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return realConnection.isWrapperFor(iface);
        }
    }
}
