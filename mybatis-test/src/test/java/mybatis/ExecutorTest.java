package mybatis;

import example.mapper.User;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ReuseExecutor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * https://mp.weixin.qq.com/s/sk0Kou9V727tRe5wddmDig
 * <p>
 * <p>3.2.4 版本之后，启用了  parameterType 参数，如果与参数类型不一致，会抛异常。
 */
class ExecutorTest {

  SqlSessionFactory sessionFactory;

  JdbcTransaction jdbcTransaction;

  Configuration configuration;

  @BeforeEach
  void before() throws IOException {

    //使用MyBatis提供的Resources类加载mybatis的配置文件
    Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
    //构建sqlSession的工厂
    sessionFactory = new SqlSessionFactoryBuilder().build(reader);

    configuration = sessionFactory.getConfiguration();
    jdbcTransaction = new JdbcTransaction(sessionFactory.openSession().getConnection());
  }

  @Test
  public void simple() throws SQLException {

    SimpleExecutor simpleExecutor = new SimpleExecutor(sessionFactory.getConfiguration(),
        jdbcTransaction);

    final MappedStatement ms = sessionFactory.getConfiguration()
        .getMappedStatement("dm.UserMapper.getUserByID");
    final BoundSql boundSql = ms.getBoundSql(1);

    simpleExecutor.doQuery(ms, 1, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, boundSql);
    simpleExecutor.doQuery(ms, 1, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, boundSql);

  }


  /**
   * update 操作生效, 需要手动 flush query 等同于 simpleExecutor
   *
   * @throws SQLException
   */
  @Test
  void batchUpdate() throws SQLException {

    BatchExecutor batchExecutor = new BatchExecutor(configuration, jdbcTransaction);

    final MappedStatement update = configuration
        .getMappedStatement("dm.UserMapper.updateName");
    final MappedStatement delete = configuration
        .getMappedStatement("dm.UserMapper.deleteById");
    final MappedStatement get = sessionFactory.getConfiguration()
        .getMappedStatement("dm.UserMapper.getUserByID");
    final MappedStatement insertUser = sessionFactory.getConfiguration()
        .getMappedStatement("dm.UserMapper.insertUser");

    // query
    batchExecutor.doUpdate(insertUser, new User().setName("" + new Date()));
    batchExecutor.doUpdate(insertUser, new User().setName("" + new Date()));

    // batch update
    User user = new User();
    user.setId(2);
    user.setName("" + new Date());
    batchExecutor.doUpdate(update, user);

    user.setId(3);
    batchExecutor.doUpdate(update, user);

    batchExecutor.doUpdate(insertUser, new User().setName("" + new Date()));

    //
    final List<BatchResult> batchResults = batchExecutor.flushStatements(false);
    jdbcTransaction.commit();
    printBatchResult(batchResults);

  }

  @Test
  void batchInsert() throws SQLException {

    BatchExecutor batchExecutor = new BatchExecutor(configuration, jdbcTransaction);

    final MappedStatement insert = configuration
        .getMappedStatement("why.demo.UserMapper.insertUser");

    User user = new User();
    user.setName("" + new Date());
    batchExecutor.doUpdate(insert, user);
    batchExecutor.doUpdate(insert, user);

    //
    final List<BatchResult> batchResults = batchExecutor.flushStatements(false);
    jdbcTransaction.commit();
    printBatchResult(batchResults);
  }

  private void printBatchResult(List<BatchResult> batchResults) {
    for (int i = 0; i < batchResults.size(); i++) {
      System.out.println(String.format("第 %d 个结果", i + 1));
      BatchResult batchResult = batchResults.get(i);
      System.out.println(Arrays.toString(batchResult.getUpdateCounts()));
    }
  }

  @Test
  void batchDelete() throws SQLException {

    BatchExecutor batchExecutor = new BatchExecutor(configuration, jdbcTransaction);

    final MappedStatement delete = configuration
        .getMappedStatement("why.demo.UserMapper.deleteById");

    User user = new User();
    user.setId((int) System.currentTimeMillis());
    batchExecutor.doUpdate(delete, user);

    user.setId((int) System.currentTimeMillis());
    batchExecutor.doUpdate(delete, user);

    //
    final List<BatchResult> batchResults = batchExecutor.flushStatements(false);
    jdbcTransaction.commit();
    printBatchResult(batchResults);

  }

  /**
   * 同一会话，相同的 sql 只有一个 prepareStatement
   *
   * @throws SQLException
   */
  @Test
  void reuseUpdate() throws SQLException {

    ReuseExecutor reuseExecutor = new ReuseExecutor(configuration, jdbcTransaction);

    final MappedStatement ms = configuration
        .getMappedStatement("why.demo.UserMapper.updateName");

    User user = new User();
    user.setId(2);
    user.setName("" + new Date());
    reuseExecutor.doUpdate(ms, user);

    user.setId(3);
    reuseExecutor.doUpdate(ms, user);

    //
    jdbcTransaction.commit();
  }

  @Test
  void reuseSelect() throws SQLException {

    ReuseExecutor reuseExecutor = new ReuseExecutor(sessionFactory.getConfiguration(),
        jdbcTransaction);

    final MappedStatement ms = sessionFactory.getConfiguration()
        .getMappedStatement("dm.UserMapper.getUserByID");
    final BoundSql boundSql = ms.getBoundSql(1);

    reuseExecutor.doQuery(ms, 1, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, boundSql);
    reuseExecutor.doQuery(ms, 1, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, boundSql);
  }

  @Test
  void reuse() throws SQLException {

    ReuseExecutor reuseExecutor = new ReuseExecutor(configuration, jdbcTransaction);

    final MappedStatement ms = configuration
        .getMappedStatement("why.demo.UserMapper.updateName");

    User user = new User();
    user.setId(2);
    user.setName("" + new Date());
    reuseExecutor.doUpdate(ms, user);

    user.setId(3);
    reuseExecutor.doUpdate(ms, user);

    //
    jdbcTransaction.commit();
  }

}