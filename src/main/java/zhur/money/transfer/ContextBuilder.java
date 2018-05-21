package zhur.money.transfer;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

public class ContextBuilder {
    public static Context create() {
        DataSource dataSource = createDataSource();
        Repository repository = new Repository();
        Service service = new Service(dataSource, repository);
        Controller controller = new Controller(service);
        return new Context(
                dataSource,
                repository,
                service,
                controller);

    }

    private static DataSource createDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setUrl("jdbc:h2:mem:db1;MODE=POSTGRESQL;INIT=RUNSCRIPT FROM 'classpath:schema.sql';DB_CLOSE_DELAY=-1");
        return ds;
    }
}
