package zhur.money.transfer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Request {
    private final String imdepotenceId;
    private final String src;
    private final String dst;
    private final long ammount;

    @JsonCreator
    public Request(
            @JsonProperty("imdepotence_id") String imdepotenceId,
            @JsonProperty("src") String src,
            @JsonProperty("dst") String dst,
            @JsonProperty("ammount") long ammount
    ) {
        this.imdepotenceId = imdepotenceId;
        this.src = src;
        this.dst = dst;
        this.ammount = ammount;
    }

    public String getImdepotenceId() {
        return imdepotenceId;
    }

    public String getSrc() {
        return src;
    }

    public String getDst() {
        return dst;
    }

    public long getAmmount() {
        return ammount;
    }
}
