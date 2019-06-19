package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuspiciousEventScore extends EventScore {

    private long term;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuspiciousEventScore)) return false;
        if (!super.equals(o)) return false;
        SuspiciousEventScore that = (SuspiciousEventScore) o;
        return term == that.term;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), term);
    }
}
