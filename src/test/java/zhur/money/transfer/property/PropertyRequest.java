package zhur.money.transfer.property;

import com.pholser.junit.quickcheck.generator.InRange;

public class PropertyRequest {
    @InRange(min="1", max="100")
    int imdepontenceId;
    @InRange(min="1", max="15")
    int src;
    @InRange(min="1", max="15")
    int dst;
    @InRange(min="-100", max="100")
    long ammount;

    @Override
    public String toString() {
        return "PropertyRequest{" +
                "imdepontenceId=" + imdepontenceId +
                ", src=" + src +
                ", dst=" + dst +
                ", ammount=" + ammount +
                '}';
    }
}
