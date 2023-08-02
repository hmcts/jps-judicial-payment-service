package uk.gov.hmcts.reform.jps.domain;

import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Service.class)
public abstract class Service_ {

	public static volatile SingularAttribute<Service, String> accountCenterCode;
	public static volatile SingularAttribute<Service, Integer> closeRecordedRecordAfterTimeInMonths;
	public static volatile SingularAttribute<Service, String> hmctsServiceId;
	public static volatile SingularAttribute<Service, Integer> retentionTimeInMonths;
	public static volatile SingularAttribute<Service, Long> id;
	public static volatile SingularAttribute<Service, String> serviceName;
	public static volatile SingularAttribute<Service, LocalDate> onboardingStartDate;

	public static final String ACCOUNT_CENTER_CODE = "accountCenterCode";
	public static final String CLOSE_RECORDED_RECORD_AFTER_TIME_IN_MONTHS = "closeRecordedRecordAfterTimeInMonths";
	public static final String HMCTS_SERVICE_ID = "hmctsServiceId";
	public static final String RETENTION_TIME_IN_MONTHS = "retentionTimeInMonths";
	public static final String ID = "id";
	public static final String SERVICE_NAME = "serviceName";
	public static final String ONBOARDING_START_DATE = "onboardingStartDate";

}

