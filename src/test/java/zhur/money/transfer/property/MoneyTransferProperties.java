package zhur.money.transfer.property;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.Fields;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.Size;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Before;
import org.junit.runner.RunWith;
import zhur.money.transfer.Context;
import zhur.money.transfer.ContextBuilder;
import zhur.money.transfer.TestHelper;
import zhur.money.transfer.model.Request;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitQuickcheck.class)
public class MoneyTransferProperties {
    public static final int INITIAL_BALANCE = 100;
    public static final int ACCOUNTS = 10;
    private Context ctx;
    private TestHelper helper;
    private List<String> accounts;

    @Before
    public void setUp() throws Exception {
        ctx = ContextBuilder.create();
        helper = new TestHelper(ctx);
        helper.clearAll();
        accounts = new ArrayList<>();
        for (int i = 0; i < ACCOUNTS; i++) {
            accounts.add(helper.generateAccount(INITIAL_BALANCE));
        }
    }

    @Property
    public void checkTransfers(
            @InRange(min = "1", max = "10") int concurrency,
            @Size(min = 1, max = 100) List<@From(Fields.class) PropertyRequest> transfers
    ) throws Exception
    {
        ExecutorService executorService = Executors.newFixedThreadPool(concurrency);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(transfers.size());
        try {
            for (PropertyRequest req : transfers) {
                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        ctx.service().transfer(createReq(req));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            finishLatch.await();
        } finally {
            executorService.shutdown();
        }

        Map<String, Long> accounts = helper.getAllAccounts();
        assertThat(accounts.values().stream().mapToLong(l -> l).sum())
                .describedAs("sum of balances equals to initial")
                .isEqualTo(INITIAL_BALANCE * ACCOUNTS);
        assertThat(accounts.values())
                .describedAs("all balances are greater or equals than zero")
                .allMatch(balance -> balance >= 0);

        try (
                Connection conn = ctx.dataSource().getConnection();
                PreparedStatement sth = conn.prepareStatement(
                        "SELECT id, sum(balance) FROM ("
                                + "SELECT id, balance FROM accounts a "
                                + "UNION ALL "
                                + "SELECT src as id, ammount as balance FROM transfers "
                                + "UNION ALL "
                                + "SELECT dst as id, -ammount as balance FROM transfers"
                                + ") as t group by id");
        )
        {
            sth.execute();
            ResultSet resultSet = sth.getResultSet();
            while (resultSet.next()) {
                assertThat(resultSet.getLong(2))
                        .describedAs("balance of {} is consistent with logged transfers", resultSet.getString(1))
                        .isEqualTo(INITIAL_BALANCE);
            }
        }
    }

    Request createReq(PropertyRequest r) {
        return new Request(
                Integer.toString(r.imdepontenceId),
                r.src >= accounts.size() ? Integer.toString(r.src) : accounts.get(r.src),
                r.dst >= accounts.size() ? Integer.toString(r.dst) : accounts.get(r.dst),
                r.ammount
        );
    }
}
