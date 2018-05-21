package zhur.money.transfer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zhur.money.transfer.model.Request;
import zhur.money.transfer.model.Response;

public class Controller {
    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Service service;

    public Controller(Service service) {
        this.service = service;
    }

    public HttpHandler createRoutes() {
        return new RoutingHandler()
                .post("/transfer", new BlockingHandler(this::transfer));
    }

    public void transfer(HttpServerExchange exch) throws Exception {
        Request req;
        try {
            req = MAPPER.readValue(exch.getInputStream(), Request.class);
        } catch (IOException e) {
            LOGGER.error("Json deserialization error", e);
            respondTo(exch, StatusCodes.BAD_REQUEST, Response.error("Invalid json"));
            return;
        }

        try {
            Response resp = service.transfer(req);
            if (resp.isSuccess()) {
                respondTo(exch, StatusCodes.OK, resp);
            } else {
                respondTo(exch, StatusCodes.BAD_REQUEST, resp);
            }
        } catch (RuntimeException e) {
            LOGGER.error("Internal error", e);
            respondTo(exch, StatusCodes.INTERNAL_SERVER_ERROR, Response.error("Internal error"));
        }
    }

    private void respondTo(HttpServerExchange exch, int code, Response resp) throws JsonProcessingException {
        exch.setStatusCode(code);
        exch.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exch.getResponseSender().send(MAPPER.writeValueAsString(resp));
    }
}
