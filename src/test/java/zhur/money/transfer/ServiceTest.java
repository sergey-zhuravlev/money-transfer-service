package zhur.money.transfer;

import java.sql.SQLException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import zhur.money.transfer.model.Request;
import zhur.money.transfer.model.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceTest {
    Context ctx;
    TestHelper helper;
    String src;
    String dst;

    @Before
    public void setUp() throws Exception {
        ctx = ContextBuilder.create();
        helper = new TestHelper(ctx);
        src = helper.generateAccount(100);
        dst = helper.generateAccount(100);
    }

    @Test
    public void incorrectNegativeAmmount() throws SQLException {
        assertError(
                ctx.service().transfer(new Request(helper.genId(), src, dst, -10))
        );
    }

    @Test
    public void sameAccountsReturnsError() throws SQLException {
        assertError(
                ctx.service().transfer(new Request(helper.genId(), src, src, 10))
        );
    }

    @Test
    public void imdepontenceIdIsRequired() throws SQLException {
        assertError(
                ctx.service().transfer(new Request("", src, dst, 10))
        );
    }

    @Test
    public void incorrectSrcError() throws SQLException {
        assertError(
                ctx.service().transfer(new Request(helper.genId(), src + "x", dst, 10))
        );
    }

    @Test
    public void incorrectDstError() throws SQLException {
        assertError(
                ctx.service().transfer(new Request(helper.genId(), src, dst + "x", 10))
        );
    }

    @Test
    public void successfulTransfer() throws SQLException {
        assertSuccess(ctx.service().transfer(new Request(helper.genId(), src, dst, 10)));
        assertThat(helper.getBalance(src)).isEqualTo(90);
        assertThat(helper.getBalance(dst)).isEqualTo(110);
    }

    @Test
    public void imdepotenceIdWorks() throws SQLException {
        String imdepotenceId = helper.genId();
        assertSuccess(ctx.service().transfer(new Request(imdepotenceId, src, dst, 10)));
        assertThat(helper.getBalance(src)).isEqualTo(90);
        assertThat(helper.getBalance(dst)).isEqualTo(110);

        assertError(ctx.service().transfer(new Request(imdepotenceId, src, dst, 10)));
    }

    @Test
    public void notEnoughMoney() throws SQLException {
        assertError(ctx.service().transfer(new Request(helper.genId(), src, dst, 101)));
    }

    private void assertSuccess(Response resp) {
        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getErrorMessage()).isNull();
    }

    private void assertError(Response resp) {
        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getErrorMessage()).isNotBlank();
    }

}
