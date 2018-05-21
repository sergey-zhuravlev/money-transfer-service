package zhur.money.transfer;

import java.sql.SQLException;

import io.undertow.Undertow;
import io.undertow.util.StatusCodes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import zhur.money.transfer.model.Request;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class RestEndpointTest {
    private Context ctx;
    private TestHelper helper;
    private String src;
    private String dst;
    private Undertow server;
    private int port;

    @Before
    public void setUp() throws Exception {
        ctx = ContextBuilder.create();
        helper = new TestHelper(ctx);
        src = helper.generateAccount(100);
        dst = helper.generateAccount(100);

        port = helper.freePort();
        server = Main.createHttpServer(port, "localhost", ctx);
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void simpleCorrectTransfer() throws SQLException {
        given().port(port).body(new Request(helper.genId(), src, dst, 20))
                .when().post("/transfer")
                .then().body("is_success", is(true));
        assertThat(helper.getBalance(src)).isEqualTo(80);
        assertThat(helper.getBalance(dst)).isEqualTo(120);
    }

    @Test
    public void simpleIncorrectTransfer() throws SQLException {
        given().port(port)
                .body(new Request(helper.genId(), src, src, 20))
                .when()
                .post("/transfer")
                .then()
                .statusCode(StatusCodes.BAD_REQUEST)
                .body("is_success", is(false))
                .body("error_message", not(isEmptyString()));
        assertThat(helper.getBalance(src)).isEqualTo(100);
        assertThat(helper.getBalance(dst)).isEqualTo(100);
    }
}
