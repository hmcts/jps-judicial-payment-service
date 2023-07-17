package uk.gov.hmcts.reform.jps.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecordingUser implements Comparable {

    @JsonAlias("userId")
    private String changeByUserId;
    @JsonAlias("userName")
    private String changeByUserName;

    @Override
    public int compareTo(@NotNull Object o) {
        RecordingUser ru = (RecordingUser) o;
        if (this.changeByUserId.compareTo(ru.getChangeByUserId()) != 0) {
            return this.changeByUserId.compareTo(ru.getChangeByUserId());
        } else {
            return this.changeByUserName.compareTo(ru.getChangeByUserName());
        }
    }
}
