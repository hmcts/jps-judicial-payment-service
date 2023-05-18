package uk.gov.hmcts.reform.jps.repository;

import org.hibernate.query.criteria.internal.OrderImpl;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.hibernate.query.criteria.internal.predicate.ComparisonPredicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.model.DateOrder;
import uk.gov.hmcts.reform.jps.model.Duration;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;

import java.time.LocalDate;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@ExtendWith(MockitoExtension.class)
class SittingRecordRepositorySearchImplTest {

    public static final String EPIMS_ID = "epimsId";
    private static final int OFF_SET = 5;
    private static final int PAGE_SIZE = 10;
    public static final String HMCTS_SERVICE_ID = "hmctsServiceId";
    public static final String SSCS = "sscs";
    public static final String REGION_ID = "regionId";
    public static final String PERSONAL_CODE = "personalCode";
    public static final String JUDGE_ROLE_TYPE_ID = "judgeRoleTypeId";
    public static final String STATUS_ID = "statusId";
    public static final String AM = "am";
    public static final String PM = "pm";
    public static final String CREATED_BY_USER_ID = "createdByUserId";
    public static final String SITTING_DATE = "sittingDate";
    @Mock
    private EntityManager entityManager;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<SittingRecord> criteriaQuery;

    @Mock
    private CriteriaQuery<Long> countCriteriaQuery;
    @Mock
    private Root<SittingRecord> sittingRecord;
    @Mock
    private TypedQuery<SittingRecord> typedQuery;

    @Mock
    private TypedQuery<Long> longTypedQuery;

    @Mock
    SingularAttributePath<String> attributePath;

    @Mock
    OrderImpl orderImpl;

    @Captor
    ArgumentCaptor<Order> order;

    @InjectMocks
    private SittingRecordRepositorySearchImpl sittingRecordRepositorySearch;

    @Test
    void verifyFindCriteriaQueryIsIntialisedCorrectlyWhenRequestHasAllValuesSet() {

        setUpMock();

        setPredicate(HMCTS_SERVICE_ID, SSCS);
        setPredicate(REGION_ID, REGION_ID);
        setPredicate(EPIMS_ID, EPIMS_ID);
        setPredicate(PERSONAL_CODE, PERSONAL_CODE);
        setPredicate(JUDGE_ROLE_TYPE_ID, JUDGE_ROLE_TYPE_ID);
        setPredicate(STATUS_ID, RECORDED.name());
        setPredicate(AM, true);
        setPredicate(PM, true);
        setPredicate(CREATED_BY_USER_ID, CREATED_BY_USER_ID);


        when(orderImpl.isAscending()).thenReturn(true);
        when(criteriaBuilder.asc(any())).thenReturn(orderImpl);

        sittingRecordRepositorySearch.find(SittingRecordSearchRequest.builder()
                                               .offset(5)
                                               .pageSize(10)
                                               .regionId(REGION_ID)
                                               .epimsId(EPIMS_ID)
                                               .dateOrder(DateOrder.ASCENDING)
                                               .dateRangeFrom(LocalDate.now().minusDays(2))
                                               .dateRangeTo(LocalDate.now())
                                               .personalCode(PERSONAL_CODE)
                                               .judgeRoleTypeId(JUDGE_ROLE_TYPE_ID)
                                               .statusId(RECORDED)
                                               .duration(Duration.FULL_DAY)
                                               .createdByUserId(CREATED_BY_USER_ID)
                                               .build(), SSCS);

        verify(entityManager)
            .getCriteriaBuilder();

        verify(typedQuery)
            .setMaxResults(PAGE_SIZE);
        verify(typedQuery)
            .setFirstResult(OFF_SET);

        verify(criteriaQuery).orderBy(order.capture());
        Order value1 = order.getValue();
        assertThat(value1.isAscending()).isTrue();

        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SSCS));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(REGION_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(EPIMS_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(PERSONAL_CODE));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(JUDGE_ROLE_TYPE_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(CREATED_BY_USER_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(RECORDED.name()));
        verify(criteriaBuilder, times(2)).equal(isA(SingularAttributePath.class), eq(true));
        verify(criteriaBuilder).between(any(), eq(LocalDate.now().minusDays(2)), eq(LocalDate.now()));
    }

    private void setUpMock() {
        when(entityManager.getCriteriaBuilder())
            .thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(SittingRecord.class))
            .thenReturn(criteriaQuery);
        when(criteriaQuery.from(SittingRecord.class))
            .thenReturn(sittingRecord);
        when(entityManager.createQuery(criteriaQuery))
            .thenReturn(typedQuery);
        when(typedQuery.setMaxResults(PAGE_SIZE))
            .thenReturn(typedQuery);
        when(typedQuery.setFirstResult(OFF_SET))
            .thenReturn(typedQuery);
        when(sittingRecord.<String>get(SITTING_DATE)).thenReturn(attributePath);
    }

    @Test
    void verifyFindCriteriaQueryIsIntialisedCorrectlyWhenRequestMandatoryValuesSetWithAmDurationAndDecendingOrdering() {

        setUpMock();

        setPredicate(HMCTS_SERVICE_ID, SSCS);
        setPredicate(REGION_ID, REGION_ID);
        setPredicate(EPIMS_ID, EPIMS_ID);
        setPredicate(AM, true);

        when(sittingRecord.<String>get(SITTING_DATE)).thenReturn(attributePath);
        when(criteriaBuilder.between(any(), isA(LocalDate.class), isA(LocalDate.class)))
            .thenReturn(mock(ComparisonPredicate.class));

        when(orderImpl.isAscending()).thenReturn(false);
        when(criteriaBuilder.desc(any())).thenReturn(orderImpl);

        sittingRecordRepositorySearch.find(SittingRecordSearchRequest.builder()
                                               .offset(5)
                                               .pageSize(10)
                                               .regionId(REGION_ID)
                                               .epimsId(EPIMS_ID)
                                               .dateOrder(DateOrder.DESCENDING)
                                               .dateRangeFrom(LocalDate.now().minusDays(2))
                                               .dateRangeTo(LocalDate.now())
                                               .duration(Duration.AM)
                                               .build(), SSCS);

        verify(entityManager)
            .getCriteriaBuilder();

        verify(typedQuery)
            .setMaxResults(PAGE_SIZE);
        verify(typedQuery)
            .setFirstResult(OFF_SET);

        verify(criteriaQuery).orderBy(order.capture());
        Order value1 = order.getValue();
        assertThat(value1.isAscending()).isFalse();

        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SSCS));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(REGION_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(EPIMS_ID));
        verify(criteriaBuilder, times(1)).equal(isA(SingularAttributePath.class), eq(true));
        verify(criteriaBuilder).between(any(), eq(LocalDate.now().minusDays(2)), eq(LocalDate.now()));
    }

    @Test
    void verifyFindCriteriaQueryIsIntialisedCorrectlyWhenRequestMandatoryValuesSetWithPmDurationAndDecendingOrdering() {
        setUpMock();

        setPredicate(HMCTS_SERVICE_ID, SSCS);
        setPredicate(REGION_ID, REGION_ID);
        setPredicate(EPIMS_ID, EPIMS_ID);
        setPredicate(PM, true);

        when(sittingRecord.<String>get(SITTING_DATE)).thenReturn(attributePath);
        when(criteriaBuilder.between(any(), isA(LocalDate.class), isA(LocalDate.class)))
            .thenReturn(mock(ComparisonPredicate.class));

        when(orderImpl.isAscending()).thenReturn(false);
        when(criteriaBuilder.desc(any())).thenReturn(orderImpl);

        sittingRecordRepositorySearch.find(SittingRecordSearchRequest.builder()
                                               .offset(5)
                                               .pageSize(10)
                                               .regionId(REGION_ID)
                                               .epimsId(EPIMS_ID)
                                               .dateOrder(DateOrder.DESCENDING)
                                               .dateRangeFrom(LocalDate.now().minusDays(2))
                                               .dateRangeTo(LocalDate.now())
                                               .duration(Duration.PM)
                                               .build(),
                                           SSCS
        );

        verify(entityManager)
            .getCriteriaBuilder();

        verify(typedQuery)
            .setMaxResults(PAGE_SIZE);
        verify(typedQuery)
            .setFirstResult(OFF_SET);

        verify(criteriaQuery).orderBy(order.capture());
        Order value1 = order.getValue();
        assertThat(value1.isAscending()).isFalse();

        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SSCS));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(REGION_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(EPIMS_ID));
        verify(criteriaBuilder, times(1)).equal(isA(SingularAttributePath.class), eq(true));
        verify(criteriaBuilder).between(any(), eq(LocalDate.now().minusDays(2)), eq(LocalDate.now()));
    }

    @Test
    void verifyTotalCriteriaQueryIsIntialisedCorrectlyWhenRequestHasAllValuesSet() {
        when(entityManager.getCriteriaBuilder())
            .thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class))
            .thenReturn(countCriteriaQuery);
        when(countCriteriaQuery.from(SittingRecord.class))
            .thenReturn(sittingRecord);
        when(entityManager.createQuery(countCriteriaQuery))
            .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(10L);

        when(sittingRecord.<String>get(SITTING_DATE)).thenReturn(attributePath);
        when(criteriaBuilder.between(any(), isA(LocalDate.class), isA(LocalDate.class)))
            .thenReturn(mock(ComparisonPredicate.class));

        setPredicate(HMCTS_SERVICE_ID, SSCS);
        setPredicate(REGION_ID, REGION_ID);
        setPredicate(EPIMS_ID, EPIMS_ID);
        setPredicate(PERSONAL_CODE, PERSONAL_CODE);
        setPredicate(JUDGE_ROLE_TYPE_ID, JUDGE_ROLE_TYPE_ID);
        setPredicate(STATUS_ID, RECORDED.name());
        setPredicate(AM, true);
        setPredicate(PM, true);
        setPredicate(CREATED_BY_USER_ID, CREATED_BY_USER_ID);


        int totalRecords = sittingRecordRepositorySearch.totalRecords(
            SittingRecordSearchRequest.builder()
                .offset(5)
                .pageSize(10)
                .regionId(REGION_ID)
                .epimsId(EPIMS_ID)
                .dateOrder(DateOrder.ASCENDING)
                .dateRangeFrom(LocalDate.now().minusDays(2))
                .dateRangeTo(LocalDate.now())
                .personalCode(PERSONAL_CODE)
                .judgeRoleTypeId(JUDGE_ROLE_TYPE_ID)
                .statusId(RECORDED)
                .duration(Duration.FULL_DAY)
                .createdByUserId(CREATED_BY_USER_ID)
                .build(),
            SSCS
        );

        assertThat(totalRecords).isEqualTo(10);

        verify(entityManager)
            .getCriteriaBuilder();

        verify(entityManager)
            .createQuery(countCriteriaQuery);

        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SSCS));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(REGION_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(EPIMS_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(PERSONAL_CODE));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(JUDGE_ROLE_TYPE_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(CREATED_BY_USER_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(RECORDED.name()));
        verify(criteriaBuilder, times(2)).equal(isA(SingularAttributePath.class), eq(true));
        verify(criteriaBuilder).between(any(), eq(LocalDate.now().minusDays(2)), eq(LocalDate.now()));
    }




    private <T> void setPredicate(String key, T value) {
        when(sittingRecord.<String>get(key)).thenReturn(attributePath);
        when(criteriaBuilder.equal(attributePath, value)).thenReturn(mock(ComparisonPredicate.class));
    }
}
