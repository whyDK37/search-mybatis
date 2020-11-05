package jdbc.java;//package jdbc.java;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public final class DataSourceUtil {

  private static final String HOST = "localhost";

  private static final int PORT = 3306;

  private static final String USER_NAME = "root";

  private static final String PASSWORD = "root";

  public static DataSource createDataSource(final String dataSourceName) {
    HikariDataSource result = new HikariDataSource();
    result.setJdbcUrl(String.format(
        "jdbc:mysql://%s:%s/%s?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8",
        HOST, PORT, dataSourceName));
    result.setUsername(USER_NAME);
    result.setPassword(PASSWORD);
    return result;
  }

  public static DataSource createDataSource(String HOST,
      String USER_NAME,
      String PASSWORD,
      int PORT,
      String dataSourceName) {
    HikariDataSource result = new HikariDataSource();
    result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
    result.setJdbcUrl(String.format(
        "jdbc:mysql://%s:%s/%s?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8",
        HOST, PORT, dataSourceName));
    result.setUsername(USER_NAME);
    result.setPassword(PASSWORD);
    return result;
  }
}