package Persistence;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBCPDataSource {

    private static BasicDataSource ds = new BasicDataSource();

//    static {
//        ds.setUrl("jdbc:postgresql://localhost:5432/maaneDBMock");
//        ds.setUsername("postgres");
//        ds.setPassword("12345");
//        ds.setMinIdle(5);
//        ds.setMaxIdle(10);
//        ds.setMaxOpenPreparedStatements(100);
//    }

    public static Connection getConnection(String url, String username, String password) throws SQLException {
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMinIdle(5);
        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(100);
        return ds.getConnection();
    }

    private DBCPDataSource(){ }
}
