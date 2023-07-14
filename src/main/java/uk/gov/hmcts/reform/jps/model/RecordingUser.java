package uk.gov.hmcts.reform.jps.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Builder
public class RecordingUser implements Comparable {

    private String userId;
    private String userName;

    @Override
    public int compareTo(@NotNull Object o) {
        RecordingUser ru = (RecordingUser) o;
        if (this.userId.compareTo(ru.getUserId()) != 0) {
            return this.userId.compareTo(ru.getUserId());
        } else {
            return this.userName.compareTo(ru.getUserName());
        }
    }
}
