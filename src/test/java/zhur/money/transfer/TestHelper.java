package zhur.money.transfer;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestHelper {
    private final Context ctx;
    private final Connection conn;

    public TestHelper(Context ctx) throws SQLException {
        this.ctx = ctx;
        conn = ctx.dataSource().getConnection();
    }

    public String genId() {
        return UUID.randomUUID().toString();
    }

    public int freePort() throws IOException {
        return new ServerSocket(0).getLocalPort();
    }

    public String generateAccount(long balance) throws SQLException {
        try (
                Connection conn = ctx.dataSource().getConnection();
                PreparedStatement st = conn.prepareStatement("INSERT INTO accounts (id, balance) VALUES (?, ?)")
        )
        {
            String id = genId();
            st.setString(1, id);
            st.setLong(2, balance);
            st.execute();
            conn.commit();
            return id;
        }
    }

    public long getBalance(String id) throws SQLException {
        try (
                Connection conn = ctx.dataSource().getConnection();
                PreparedStatement sth = conn.prepareStatement("SELECT balance FROM accounts WHERE id = ?")
        )
        {
            sth.setString(1, id);
            sth.execute();
            ResultSet resultSet = sth.getResultSet();
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    public void clearAll() throws Exception {
        try (
                Connection conn = ctx.dataSource().getConnection();
                Statement st = conn.createStatement();
        )
        {
            st.execute("DELETE FROM accounts");
            st.execute("DELETE FROM transfers");
        }
    }

    public Map<String, Long> getAllAccounts() throws SQLException {
        Map<String, Long> ret = new HashMap<>();
        try (
                Connection conn = ctx.dataSource().getConnection();
                PreparedStatement sth = conn.prepareStatement("SELECT id, balance FROM accounts")
        )
        {
            sth.execute();
            ResultSet resultSet = sth.getResultSet();
            while (resultSet.next()) {
                ret.put(resultSet.getString(1), resultSet.getLong(2));
            }
        }
        return ret;
    }
}
