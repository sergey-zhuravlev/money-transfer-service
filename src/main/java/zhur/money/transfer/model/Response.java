package zhur.money.transfer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response {
    private static final Response SUCCESS = new Response(true, null);

    private final boolean isSuccess;
    private final String errorMessage;

    public Response(boolean isSuccess, String errorMessage) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }

    public static Response success() {
        return SUCCESS;
    }

    public static Response error(String errorMessage) {
        return new Response(false, errorMessage);
    }

    @JsonProperty("is_success")
    public boolean isSuccess() {
        return isSuccess;
    }

    @JsonProperty("error_message")
    public String getErrorMessage() {
        return errorMessage;
    }
}
