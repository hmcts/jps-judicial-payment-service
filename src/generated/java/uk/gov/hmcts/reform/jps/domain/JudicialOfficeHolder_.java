package uk.gov.hmcts.reform.jps.domain;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(JudicialOfficeHolder.class)
public abstract class JudicialOfficeHolder_ {

	public static volatile SingularAttribute<JudicialOfficeHolder, String> personalCode;
	public static volatile ListAttribute<JudicialOfficeHolder, JohPayroll> johPayrolls;
	public static volatile SingularAttribute<JudicialOfficeHolder, Long> id;

	public static final String PERSONAL_CODE = "personalCode";
	public static final String JOH_PAYROLLS = "johPayrolls";
	public static final String ID = "id";

}

