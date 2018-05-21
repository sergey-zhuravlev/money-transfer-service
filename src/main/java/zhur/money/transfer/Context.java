package zhur.money.transfer;

import javax.sql.DataSource;

public class Context {
    private final DataSource dataSource;
    private final Repository repository;
    private final Service service;
    private final Controller controller;

    public Context(DataSource dataSource, Repository repository,
            Service service, Controller controller)
    {
        this.dataSource = dataSource;
        this.repository = repository;
        this.service = service;
        this.controller = controller;
    }

    public DataSource dataSource() {
        return dataSource;
    }

    public Repository repository() {
        return repository;
    }

    public Controller controller() {
        return controller;
    }

    public Service service() {
        return service;
    }
}
