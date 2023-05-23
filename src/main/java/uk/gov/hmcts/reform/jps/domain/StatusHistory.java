package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.*;

@Builder
@NoArgsConstructor()
@AllArgsConstructor
@Data
@Entity
@Table(name = "status_history")
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_history_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sitting_record_ID")
    private Long sittingRecordId;

    @Column(name = "status_ID")
    private String statusID;

    @Column(name = "change_date_time")
    private LocalDateTime changeDateTime;

    @Column(name = "change_by_user_id")
    private String changeByUserId;

}
