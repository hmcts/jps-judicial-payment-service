package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(exclude = "exportedFileDataHeader")
@Entity
@Table(name = "exported_files")
public class ExportedFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exported_file_id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exported_group_id", nullable = false, updatable = false)
    private ExportedFileDataHeader exportedFileDataHeader;

    @Column(name = "exported_date_time")
    private LocalDateTime exportedDateTime;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_record_count")
    private Integer fileRecordCount;
}
