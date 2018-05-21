package zhur.money.transfer;

import io.undertow.Undertow;

public class Main {
    public static void main(String[] args) throws Exception {
        Context ctx = ContextBuilder.create();
        createHttpServer(8080, "localhost", ctx)
                .start();
    }

    public static Undertow createHttpServer(int port, String host, Context ctx) {
        return Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(ctx.controller().createRoutes())
                .build();
    }
}
