package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Builder
@NoArgsConstructor()
@AllArgsConstructor
@Data
@Entity
@Table(name = "status_history")
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sitting_record_id")
    private SittingRecord sittingRecordId;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "change_date_time")
    private LocalDateTime changeDateTime;

    @Column(name = "change_by_user_id")
    private String changeByUserId;

    @Column(name = "change_by_name")
    private String changeByName;

}
