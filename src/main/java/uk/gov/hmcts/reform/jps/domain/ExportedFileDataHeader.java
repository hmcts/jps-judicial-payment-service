package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "exported_file_data_header")
public class ExportedFileDataHeader {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exported_group_id")
    private Long id;

    @Column(name = "exported_date_time")
    private LocalDateTime exportedDateTime;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "exported_by")
    private String exportedBy;

    @Column(name = "status")
    private String status;

    @Column(name = "hmcts_service_id")
    private String hmctsServiceId;


}
