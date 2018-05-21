package zhur.money.transfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.OptionalLong;

public class Repository {
    public Repository() {
    }

    public OptionalLong getBalanceAndLock(Connection conn, String id) throws SQLException {
        try (PreparedStatement sth = conn.prepareStatement("SELECT balance FROM accounts WHERE id = ? FOR UPDATE")) {
            sth.setString(1, id);
            sth.execute();
            ResultSet resultSet = sth.getResultSet();
            if (!resultSet.next()) {
                return OptionalLong.empty();
            }
            long balance = resultSet.getLong(1);
            if (resultSet.next()) {
                throw new IllegalStateException("More than one row returned");
            }
            return OptionalLong.of(balance);
        }
    }

    public boolean tryInsertTransfer(Connection conn, String imdepontenceId, String src, String dst, long ammount)
            throws SQLException
    {
        try (
                PreparedStatement sth = conn.prepareStatement(
                        "INSERT INTO transfers (imdepontence_id, src, dst, ammount) "
                                + "SELECT ?, ?, ?, ? "
                                + "WHERE NOT EXISTS (SELECT 1 FROM transfers WHERE imdepontence_id = ?)");
        )
        {
            sth.setString(1, imdepontenceId);
            sth.setString(2, src);
            sth.setString(3, dst);
            sth.setLong(4, ammount);
            sth.setString(5, imdepontenceId);
            int insertedRows = sth.executeUpdate();
            return insertedRows > 0;
        }
    }

    public void makeTransfer(Connection conn, String src, String dst, long ammount)
            throws SQLException
    {
        try (
                PreparedStatement update = conn.prepareStatement(
                        "UPDATE accounts SET balance = balance + ? WHERE id = ?");
        )
        {
            update.setLong(1, -ammount);
            update.setString(2, src);
            update.execute();

            update.setLong(1, ammount);
            update.setString(2, dst);
            update.execute();
        }
    }
}
