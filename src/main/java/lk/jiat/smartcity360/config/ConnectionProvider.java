package lk.jiat.smartcity360.config;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionProvider {
    private static final String JNDI_NAME = "java:comp/env/jdbc/SmartCity360";

    private ConnectionProvider() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            DataSource dataSource = (DataSource) new InitialContext().lookup(JNDI_NAME);
            return dataSource.getConnection();
        } catch (NamingException exception) {
            throw new SQLException("Unable to find the configured JDBC resource " + JNDI_NAME, exception);
        }
    }
}
