package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Builder
@NoArgsConstructor()
@AllArgsConstructor
@Data
@ToString
@Entity
@Table(name = "judicial_office_holder")
public class JudicialOfficeHolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "local_joh_record_id")
    private Long id;

    @Column(name = "personal_code")
    private String personalCode;

}
