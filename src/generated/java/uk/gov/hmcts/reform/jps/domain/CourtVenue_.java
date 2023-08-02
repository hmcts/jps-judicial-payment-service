package uk.gov.hmcts.reform.jps.domain;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CourtVenue.class)
public abstract class CourtVenue_ {

	public static volatile SingularAttribute<CourtVenue, String> epimmsId;
	public static volatile SingularAttribute<CourtVenue, String> costCenterCode;
	public static volatile SingularAttribute<CourtVenue, String> hmctsServiceId;
	public static volatile SingularAttribute<CourtVenue, Long> id;

	public static final String EPIMMS_ID = "epimmsId";
	public static final String COST_CENTER_CODE = "costCenterCode";
	public static final String HMCTS_SERVICE_ID = "hmctsServiceId";
	public static final String ID = "id";

}

