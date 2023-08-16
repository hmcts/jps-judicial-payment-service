package uk.gov.hmcts.reform.jps.domain;

import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(JohPayroll.class)
public abstract class JohPayroll_ {

	public static volatile SingularAttribute<JohPayroll, JudicialOfficeHolder> judicialOfficeHolder;
	public static volatile SingularAttribute<JohPayroll, String> judgeRoleTypeId;
	public static volatile SingularAttribute<JohPayroll, LocalDate> effectiveStartDate;
	public static volatile SingularAttribute<JohPayroll, Long> id;
	public static volatile SingularAttribute<JohPayroll, String> payrollId;

	public static final String JUDICIAL_OFFICE_HOLDER = "judicialOfficeHolder";
	public static final String JUDGE_ROLE_TYPE_ID = "judgeRoleTypeId";
	public static final String EFFECTIVE_START_DATE = "effectiveStartDate";
	public static final String ID = "id";
	public static final String PAYROLL_ID = "payrollId";

}

