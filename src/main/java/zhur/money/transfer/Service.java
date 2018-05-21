package zhur.money.transfer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.OptionalLong;

import javax.sql.DataSource;

import zhur.money.transfer.model.Request;
import zhur.money.transfer.model.Response;

public class Service {
    private final DataSource dataSource;
    private final Repository repository;

    public Service(DataSource dataSource, Repository repository) {
        this.dataSource = dataSource;
        this.repository = repository;
    }

    public Response transfer(Request req) throws SQLException {
        Response resp = validateRequest(req);
        if (!resp.isSuccess()) {
            return resp;
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (repository.isTransferAlreadyProcessed(conn, req.getImdepotenceId())) {
                    return Response.error("Transfer with such imdepontence_id already processed");
                }

                OptionalLong srcAmmount, dstAmmount;
                if (req.getSrc().compareTo(req.getDst()) < 0) {
                    srcAmmount = repository.getBalanceAndLock(conn, req.getSrc());
                    dstAmmount = repository.getBalanceAndLock(conn, req.getDst());
                } else {
                    dstAmmount = repository.getBalanceAndLock(conn, req.getDst());
                    srcAmmount = repository.getBalanceAndLock(conn, req.getSrc());
                }

                if (!srcAmmount.isPresent()) {
                    return Response.error("'src' account not found");
                }
                if (!dstAmmount.isPresent()) {
                    return Response.error("'dst' account not found");
                }
                if (req.getAmmount() > srcAmmount.getAsLong()) {
                    return Response.error("No enough funds on src account");
                }

                repository.makeTransfer(conn, req.getImdepotenceId(), req.getSrc(), req.getDst(), req.getAmmount());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }

        return Response.success();
    }

    private Response validateRequest(Request req) {
        if (req.getImdepotenceId() == null || req.getImdepotenceId().isEmpty()) {
            return Response.error("Empty 'imdepotence_id'");
        }
        if (req.getSrc() == null || req.getSrc().isEmpty()) {
            return Response.error("Empty 'src'");
        }
        if (req.getDst() == null || req.getDst().isEmpty()) {
            return Response.error("Empty 'dst'");
        }

        if (req.getSrc().equals(req.getDst())) {
            return Response.error("'src' and 'dst' should be different");
        }

        if (req.getAmmount() <= 0) {
            return Response.error("'ammount' should be greater or equesls than 0");
        }

        return Response.success();
    }
}
