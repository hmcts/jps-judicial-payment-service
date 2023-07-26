package uk.gov.hmcts.reform.jps.domain;

import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import uk.gov.hmcts.reform.jps.model.StatusId;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SittingRecord.class)
public abstract class SittingRecord_ {

	public static volatile SingularAttribute<SittingRecord, String> epimmsId;
	public static volatile SingularAttribute<SittingRecord, String> personalCode;
	public static volatile ListAttribute<SittingRecord, StatusHistory> statusHistories;
	public static volatile SingularAttribute<SittingRecord, StatusId> statusId;
	public static volatile SingularAttribute<SittingRecord, String> regionId;
	public static volatile SingularAttribute<SittingRecord, String> hmctsServiceId;
	public static volatile SingularAttribute<SittingRecord, String> judgeRoleTypeId;
	public static volatile SingularAttribute<SittingRecord, Long> id;
	public static volatile SingularAttribute<SittingRecord, Boolean> am;
	public static volatile SingularAttribute<SittingRecord, Boolean> pm;
	public static volatile SingularAttribute<SittingRecord, Long> contractTypeId;
	public static volatile SingularAttribute<SittingRecord, LocalDate> sittingDate;

	public static final String EPIMMS_ID = "epimmsId";
	public static final String PERSONAL_CODE = "personalCode";
	public static final String STATUS_HISTORIES = "statusHistories";
	public static final String STATUS_ID = "statusId";
	public static final String REGION_ID = "regionId";
	public static final String HMCTS_SERVICE_ID = "hmctsServiceId";
	public static final String JUDGE_ROLE_TYPE_ID = "judgeRoleTypeId";
	public static final String ID = "id";
	public static final String AM = "am";
	public static final String PM = "pm";
	public static final String CONTRACT_TYPE_ID = "contractTypeId";
	public static final String SITTING_DATE = "sittingDate";

}

