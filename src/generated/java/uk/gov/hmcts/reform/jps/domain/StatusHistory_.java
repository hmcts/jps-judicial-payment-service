package uk.gov.hmcts.reform.jps.domain;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import uk.gov.hmcts.reform.jps.model.StatusId;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(StatusHistory.class)
public abstract class StatusHistory_ {

	public static volatile SingularAttribute<StatusHistory, String> changeByName;
	public static volatile SingularAttribute<StatusHistory, StatusId> statusId;
	public static volatile SingularAttribute<StatusHistory, Long> id;
	public static volatile SingularAttribute<StatusHistory, SittingRecord> sittingRecord;
	public static volatile SingularAttribute<StatusHistory, String> changeByUserId;
	public static volatile SingularAttribute<StatusHistory, LocalDateTime> changeDateTime;

	public static final String CHANGE_BY_NAME = "changeByName";
	public static final String STATUS_ID = "statusId";
	public static final String ID = "id";
	public static final String SITTING_RECORD = "sittingRecord";
	public static final String CHANGE_BY_USER_ID = "changeByUserId";
	public static final String CHANGE_DATE_TIME = "changeDateTime";

}

