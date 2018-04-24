package azurevote.redis.models;

import java.io.Serializable;

public class VoteCount implements Serializable {
    private final String option;
    private final String count;

    public VoteCount(String option, String count) {
        this.count = count;
        this.option = option;
    }

    public String getCount() {
        return count;
    }

    public String getOption() {
        return option;
    }

    @Override
    public String toString() {
        return "VoteCount{" + "option='" + option + '\'' + ", count='" + count + '}';
    }
}