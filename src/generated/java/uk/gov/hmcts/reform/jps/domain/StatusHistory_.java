package uk.gov.hmcts.reform.jps.domain;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(StatusHistory.class)
public abstract class StatusHistory_ {

	public static volatile SingularAttribute<StatusHistory, LocalDateTime> changedDateTime;
	public static volatile SingularAttribute<StatusHistory, String> statusId;
	public static volatile SingularAttribute<StatusHistory, String> changedByUserId;
	public static volatile SingularAttribute<StatusHistory, Long> id;
	public static volatile SingularAttribute<StatusHistory, SittingRecord> sittingRecord;
	public static volatile SingularAttribute<StatusHistory, String> changedByName;

	public static final String CHANGED_DATE_TIME = "changedDateTime";
	public static final String STATUS_ID = "statusId";
	public static final String CHANGED_BY_USER_ID = "changedByUserId";
	public static final String ID = "id";
	public static final String SITTING_RECORD = "sittingRecord";
	public static final String CHANGED_BY_NAME = "changedByName";

}

