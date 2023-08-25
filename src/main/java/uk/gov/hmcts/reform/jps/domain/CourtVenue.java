package uk.gov.hmcts.reform.jps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Getter
@Setter
@ToString
@Entity
@Table(name = "court_venue")
public class CourtVenue {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "local_court_venue_record_id")
    private Long id;

    @Column(name =  "epimms_id")
    private String epimmsId;

    @Column(name = "hmcts_service_id")
    private String hmctsServiceId;

    @Column(name = "cost_center_code")
    private String costCenterCode;
}
