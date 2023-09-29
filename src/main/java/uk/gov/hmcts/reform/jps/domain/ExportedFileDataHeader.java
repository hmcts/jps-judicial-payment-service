package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(exclude = "exportedFiles")
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

    @ToString.Exclude
    @OneToMany(
        mappedBy = "exportedFileDataHeader",
        cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
        orphanRemoval = true
    )
    private final Set<ExportedFile> exportedFiles = new HashSet<>();

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "exported_by")
    private String exportedBy;

    @Column(name = "status")
    private String status;

    @Column(name = "hmcts_service_id")
    private String hmctsServiceId;

    public void addExportedFile(ExportedFile exportedFile) {
        exportedFile.setExportedFileDataHeader(this);
        exportedFiles.add(exportedFile);
    }
}
