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
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecord_;
import uk.gov.hmcts.reform.jps.model.DateOrder;
import uk.gov.hmcts.reform.jps.model.Duration;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;

import java.time.LocalDate;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
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

    private static final int OFF_SET = 5;
    private static final int PAGE_SIZE = 10;
    public static final String SSCS = "sscs";

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
    @Mock
    Predicate predicate;
    @Captor
    ArgumentCaptor<Order> order;
    @InjectMocks
    private SittingRecordRepositorySearchImpl sittingRecordRepositorySearch;

    @Test
    @Sql(scripts = {"classpath:insert_service_test_data.sql"})
    void verifyFindCriteriaQueryIsInitialisedCorrectlyWhenRequestHasAllValuesSet() {

        setUpMock();

        when(sittingRecord.<String>get(SittingRecord_.SITTING_DATE)).thenReturn(attributePath);
        when(criteriaBuilder.between(any(), isA(LocalDate.class), isA(LocalDate.class))).thenReturn(predicate);

        when(sittingRecord.<String>get(SittingRecord_.STATUS_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.HMCTS_SERVICE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.REGION_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.EPIMMS_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PERSONAL_CODE)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.JUDGE_ROLE_TYPE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.AM)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PM)).thenReturn(attributePath);

        when(criteriaBuilder.equal(attributePath, RECORDED)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SSCS)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.REGION_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.EPIMMS_ID))
            .thenReturn(mock(ComparisonPredicate.class));
        when(criteriaBuilder.equal(attributePath, SittingRecord_.PERSONAL_CODE)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.JUDGE_ROLE_TYPE_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, true)).thenReturn(predicate);

        when(orderImpl.isAscending()).thenReturn(true);
        when(criteriaBuilder.asc(any())).thenReturn(orderImpl);

        sittingRecordRepositorySearch.find(SittingRecordSearchRequest.builder()
                                               .offset(5)
                                               .pageSize(10)
                                               .regionId(SittingRecord_.REGION_ID)
                                               .epimmsId(SittingRecord_.EPIMMS_ID)
                                               .dateOrder(DateOrder.ASCENDING)
                                               .dateRangeFrom(LocalDate.now().minusDays(2))
                                               .dateRangeTo(LocalDate.now())
                                               .personalCode(SittingRecord_.PERSONAL_CODE)
                                               .judgeRoleTypeId(SittingRecord_.JUDGE_ROLE_TYPE_ID)
                                               .statusId(RECORDED)
                                               .duration(Duration.FULL_DAY)
                                               .build(), SSCS);

        verify(entityManager).getCriteriaBuilder();
        verify(typedQuery).setMaxResults(PAGE_SIZE);
        verify(typedQuery).setFirstResult(OFF_SET);
        verify(criteriaQuery).orderBy(order.capture());

        Order value1 = order.getValue();
        assertThat(value1.isAscending()).isTrue();

        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SSCS));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.REGION_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.EPIMMS_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.PERSONAL_CODE));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.JUDGE_ROLE_TYPE_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(RECORDED));
        verify(criteriaBuilder, times(2)).equal(isA(SingularAttributePath.class), eq(true));
        verify(criteriaBuilder).between(any(), eq(LocalDate.now().minusDays(2)), eq(LocalDate.now()));
    }

    @Test
    void verifyFindCriteriaQueryIsInitialisedCorrectlyWhenRequestHasNoRegionIdSelected() {

        setUpMock();

        when(sittingRecord.<String>get(SittingRecord_.SITTING_DATE)).thenReturn(attributePath);
        when(criteriaBuilder.between(any(), isA(LocalDate.class), isA(LocalDate.class))).thenReturn(predicate);

        when(sittingRecord.<String>get(SittingRecord_.STATUS_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.HMCTS_SERVICE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.EPIMS_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PERSONAL_CODE)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.JUDGE_ROLE_TYPE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.AM)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PM)).thenReturn(attributePath);

        when(criteriaBuilder.equal(attributePath, RECORDED.name())).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SSCS)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.EPIMS_ID)).thenReturn(mock(ComparisonPredicate.class));
        when(criteriaBuilder.equal(attributePath, SittingRecord_.PERSONAL_CODE)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.JUDGE_ROLE_TYPE_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, true)).thenReturn(predicate);

        when(orderImpl.isAscending()).thenReturn(true);
        when(criteriaBuilder.asc(any())).thenReturn(orderImpl);

        sittingRecordRepositorySearch.find(SittingRecordSearchRequest.builder()
                                               .offset(5)
                                               .pageSize(10)
                                               .epimsId(SittingRecord_.EPIMS_ID)
                                               .dateOrder(DateOrder.ASCENDING)
                                               .dateRangeFrom(LocalDate.now().minusDays(2))
                                               .dateRangeTo(LocalDate.now())
                                               .personalCode(SittingRecord_.PERSONAL_CODE)
                                               .judgeRoleTypeId(SittingRecord_.JUDGE_ROLE_TYPE_ID)
                                               .statusId(RECORDED)
                                               .duration(Duration.FULL_DAY)
                                               .build(), SSCS);

        verify(entityManager).getCriteriaBuilder();
        verify(typedQuery).setMaxResults(PAGE_SIZE);
        verify(typedQuery).setFirstResult(OFF_SET);
        verify(criteriaQuery).orderBy(order.capture());

        Order value1 = order.getValue();
        assertThat(value1.isAscending()).isTrue();

        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SSCS));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.EPIMS_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.PERSONAL_CODE));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.JUDGE_ROLE_TYPE_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(RECORDED.name()));
        verify(criteriaBuilder, times(2)).equal(isA(SingularAttributePath.class), eq(true));
        verify(criteriaBuilder).between(any(), eq(LocalDate.now().minusDays(2)), eq(LocalDate.now()));
    }

    @Test
    void verifyFindCriteriaQueryIsInitialisedCorrectlyWhenRequestHasNoEpimmsIdSelected() {

        setUpMock();

        when(sittingRecord.<String>get(SittingRecord_.SITTING_DATE)).thenReturn(attributePath);
        when(criteriaBuilder.between(any(), isA(LocalDate.class), isA(LocalDate.class))).thenReturn(predicate);

        when(sittingRecord.<String>get(SittingRecord_.STATUS_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.HMCTS_SERVICE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.REGION_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PERSONAL_CODE)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.JUDGE_ROLE_TYPE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.AM)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PM)).thenReturn(attributePath);

        when(criteriaBuilder.equal(attributePath, RECORDED.name())).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SSCS)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.REGION_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.PERSONAL_CODE)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.JUDGE_ROLE_TYPE_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, true)).thenReturn(predicate);

        when(orderImpl.isAscending()).thenReturn(true);
        when(criteriaBuilder.asc(any())).thenReturn(orderImpl);

        sittingRecordRepositorySearch.find(SittingRecordSearchRequest.builder()
                                               .offset(5)
                                               .pageSize(10)
                                               .regionId(SittingRecord_.REGION_ID)
                                               .dateOrder(DateOrder.ASCENDING)
                                               .dateRangeFrom(LocalDate.now().minusDays(2))
                                               .dateRangeTo(LocalDate.now())
                                               .personalCode(SittingRecord_.PERSONAL_CODE)
                                               .judgeRoleTypeId(SittingRecord_.JUDGE_ROLE_TYPE_ID)
                                               .statusId(RECORDED)
                                               .duration(Duration.FULL_DAY)
                                               .build(), SSCS);

        verify(entityManager).getCriteriaBuilder();
        verify(typedQuery).setMaxResults(PAGE_SIZE);
        verify(typedQuery).setFirstResult(OFF_SET);
        verify(criteriaQuery).orderBy(order.capture());

        Order value1 = order.getValue();
        assertThat(value1.isAscending()).isTrue();

        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SSCS));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.REGION_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.PERSONAL_CODE));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.JUDGE_ROLE_TYPE_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(RECORDED.name()));
        verify(criteriaBuilder, times(2)).equal(isA(SingularAttributePath.class), eq(true));
        verify(criteriaBuilder).between(any(), eq(LocalDate.now().minusDays(2)), eq(LocalDate.now()));
    }

    @Test
    void verifyFindCriteriaQueryIsInitialisedCorrectlyWhenRequestHasNoEpimmsAndNoRegionIdSelected() {

        setUpMock();

        when(sittingRecord.<String>get(SittingRecord_.SITTING_DATE)).thenReturn(attributePath);
        when(criteriaBuilder.between(any(), isA(LocalDate.class), isA(LocalDate.class))).thenReturn(predicate);

        when(sittingRecord.<String>get(SittingRecord_.STATUS_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.HMCTS_SERVICE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PERSONAL_CODE)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.JUDGE_ROLE_TYPE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.AM)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PM)).thenReturn(attributePath);

        when(criteriaBuilder.equal(attributePath, RECORDED.name())).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SSCS)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.PERSONAL_CODE)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.JUDGE_ROLE_TYPE_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, true)).thenReturn(predicate);

        when(orderImpl.isAscending()).thenReturn(true);
        when(criteriaBuilder.asc(any())).thenReturn(orderImpl);

        sittingRecordRepositorySearch.find(SittingRecordSearchRequest.builder()
                                               .offset(5)
                                               .pageSize(10)
                                               .dateOrder(DateOrder.ASCENDING)
                                               .dateRangeFrom(LocalDate.now().minusDays(2))
                                               .dateRangeTo(LocalDate.now())
                                               .personalCode(SittingRecord_.PERSONAL_CODE)
                                               .judgeRoleTypeId(SittingRecord_.JUDGE_ROLE_TYPE_ID)
                                               .statusId(RECORDED)
                                               .duration(Duration.FULL_DAY)
                                               .build(), SSCS);

        verify(entityManager).getCriteriaBuilder();
        verify(typedQuery).setMaxResults(PAGE_SIZE);
        verify(typedQuery).setFirstResult(OFF_SET);
        verify(criteriaQuery).orderBy(order.capture());

        Order value1 = order.getValue();
        assertThat(value1.isAscending()).isTrue();

        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SSCS));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.PERSONAL_CODE));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.JUDGE_ROLE_TYPE_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(RECORDED.name()));
        verify(criteriaBuilder, times(2)).equal(isA(SingularAttributePath.class), eq(true));
        verify(criteriaBuilder).between(any(), eq(LocalDate.now().minusDays(2)), eq(LocalDate.now()));
    }

    @Test
    void testFindCriteriaQueryIsInitialisedCorrectlyWhenRequestMandatoryValuesSetWithAmDurationAndDescendingOrdering() {
        setUpMock();

        when(sittingRecord.<String>get(SittingRecord_.SITTING_DATE)).thenReturn(attributePath);
        when(criteriaBuilder.between(any(), isA(LocalDate.class), isA(LocalDate.class))).thenReturn(predicate);

        when(sittingRecord.<String>get(SittingRecord_.HMCTS_SERVICE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.REGION_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.EPIMMS_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.AM)).thenReturn(attributePath);

        when(criteriaBuilder.equal(attributePath, SSCS)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.REGION_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.EPIMMS_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, true)).thenReturn(predicate);

        when(orderImpl.isAscending()).thenReturn(false);
        when(criteriaBuilder.desc(any())).thenReturn(orderImpl);
        sittingRecordRepositorySearch.find(SittingRecordSearchRequest.builder()
                                               .offset(5)
                                               .pageSize(10)
                                               .regionId(SittingRecord_.REGION_ID)
                                               .epimmsId(SittingRecord_.EPIMMS_ID)
                                               .dateOrder(DateOrder.DESCENDING)
                                               .dateRangeFrom(LocalDate.now().minusDays(2))
                                               .dateRangeTo(LocalDate.now())
                                               .duration(Duration.AM)
                                               .build(), SSCS);

        verify(entityManager).getCriteriaBuilder();
        verify(typedQuery).setMaxResults(PAGE_SIZE);
        verify(typedQuery).setFirstResult(OFF_SET);
        verify(criteriaQuery).orderBy(order.capture());

        Order value1 = order.getValue();
        assertThat(value1.isAscending()).isFalse();

        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SSCS));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.REGION_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.EPIMMS_ID));
        verify(criteriaBuilder, times(1))
            .equal(isA(SingularAttributePath.class), eq(true));
        verify(criteriaBuilder).between(any(), eq(LocalDate.now().minusDays(2)), eq(LocalDate.now()));
    }

    @Test
    void testFindCriteriaQueryIsInitialisedCorrectlyWhenRequestMandatoryValuesSetWithPmDurationAndDescendingOrdering() {
        setUpMock();
        when(sittingRecord.<String>get(SittingRecord_.SITTING_DATE)).thenReturn(attributePath);
        when(criteriaBuilder.between(any(), isA(LocalDate.class), isA(LocalDate.class))).thenReturn(predicate);

        when(sittingRecord.<String>get(SittingRecord_.HMCTS_SERVICE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.REGION_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.EPIMMS_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PM)).thenReturn(attributePath);

        when(criteriaBuilder.equal(attributePath, SSCS)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.REGION_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.EPIMMS_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, true)).thenReturn(predicate);

        when(orderImpl.isAscending()).thenReturn(false);
        when(criteriaBuilder.desc(any())).thenReturn(orderImpl);

        sittingRecordRepositorySearch.find(SittingRecordSearchRequest.builder()
                                               .offset(5)
                                               .pageSize(10)
                                               .regionId(SittingRecord_.REGION_ID)
                                               .epimmsId(SittingRecord_.EPIMMS_ID)
                                               .dateOrder(DateOrder.DESCENDING)
                                               .dateRangeFrom(LocalDate.now().minusDays(2))
                                               .dateRangeTo(LocalDate.now())
                                               .duration(Duration.PM)
                                               .build(),
                                           SSCS
        );

        verify(entityManager).getCriteriaBuilder();
        verify(typedQuery).setMaxResults(PAGE_SIZE);
        verify(typedQuery).setFirstResult(OFF_SET);
        verify(criteriaQuery).orderBy(order.capture());

        Order value1 = order.getValue();
        assertThat(value1.isAscending()).isFalse();

        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SSCS));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.REGION_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.EPIMMS_ID));
        verify(criteriaBuilder, times(1))
            .equal(isA(SingularAttributePath.class), eq(true));
        verify(criteriaBuilder).between(any(), eq(LocalDate.now().minusDays(2)), eq(LocalDate.now()));
    }

    @Test
    void verifyTotalCriteriaQueryIsInitialisedCorrectlyWhenRequestHasAllValuesSet() {
        when(entityManager.getCriteriaBuilder())
            .thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class))
            .thenReturn(countCriteriaQuery);
        when(countCriteriaQuery.from(SittingRecord.class))
            .thenReturn(sittingRecord);
        when(entityManager.createQuery(countCriteriaQuery))
            .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(10L);

        when(sittingRecord.<String>get(SittingRecord_.STATUS_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.HMCTS_SERVICE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.REGION_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.EPIMMS_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PERSONAL_CODE)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.JUDGE_ROLE_TYPE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.SITTING_DATE)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.AM)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PM)).thenReturn(attributePath);

        when(criteriaBuilder.equal(attributePath, RECORDED)).thenReturn(mock(ComparisonPredicate.class));
        when(criteriaBuilder.equal(attributePath, SSCS)).thenReturn(mock(ComparisonPredicate.class));
        when(criteriaBuilder.equal(attributePath, SittingRecord_.REGION_ID))
            .thenReturn(mock(ComparisonPredicate.class));
        when(criteriaBuilder.equal(attributePath, SittingRecord_.EPIMMS_ID))
            .thenReturn(mock(ComparisonPredicate.class));
        when(criteriaBuilder.equal(attributePath, SittingRecord_.PERSONAL_CODE))
            .thenReturn(mock(ComparisonPredicate.class));
        when(criteriaBuilder.equal(attributePath, SittingRecord_.JUDGE_ROLE_TYPE_ID))
            .thenReturn(mock(ComparisonPredicate.class));
        when(criteriaBuilder.equal(attributePath, true)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, true)).thenReturn(predicate);

        int totalRecords = sittingRecordRepositorySearch.totalRecords(
            SittingRecordSearchRequest.builder()
                .offset(5)
                .pageSize(10)
                .regionId(SittingRecord_.REGION_ID)
                .epimmsId(SittingRecord_.EPIMMS_ID)
                .dateOrder(DateOrder.ASCENDING)
                .dateRangeFrom(LocalDate.now().minusDays(2))
                .dateRangeTo(LocalDate.now())
                .personalCode(SittingRecord_.PERSONAL_CODE)
                .judgeRoleTypeId(SittingRecord_.JUDGE_ROLE_TYPE_ID)
                .statusId(RECORDED)
                .duration(Duration.FULL_DAY)
                .build(),
            SSCS
        );

        assertThat(totalRecords).isEqualTo(10);

        verify(entityManager).getCriteriaBuilder();

        verify(entityManager).createQuery(countCriteriaQuery);

        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SSCS));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.REGION_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.EPIMMS_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.PERSONAL_CODE));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.JUDGE_ROLE_TYPE_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(RECORDED));
        verify(criteriaBuilder, times(2))
            .equal(isA(SingularAttributePath.class), eq(true));
        verify(criteriaBuilder).between(any(), eq(LocalDate.now().minusDays(2)), eq(LocalDate.now()));
    }

    @Test
    void checkTotalCriteriaQueryIsInitialisedCorrectlyWhenRequestHasAllValuesSet() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(countCriteriaQuery);
        when(countCriteriaQuery.from(SittingRecord.class)).thenReturn(sittingRecord);
        when(entityManager.createQuery(countCriteriaQuery)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(10L);
        when(criteriaBuilder.between(any(), isA(LocalDate.class), isA(LocalDate.class)))
            .thenReturn(predicate);

        when(sittingRecord.<String>get(SittingRecord_.STATUS_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.HMCTS_SERVICE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.REGION_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.EPIMMS_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PERSONAL_CODE)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.JUDGE_ROLE_TYPE_ID)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.SITTING_DATE)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.AM)).thenReturn(attributePath);
        when(sittingRecord.<String>get(SittingRecord_.PM)).thenReturn(attributePath);

        when(criteriaBuilder.equal(attributePath, RECORDED)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SSCS)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.REGION_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.EPIMMS_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.PERSONAL_CODE)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, SittingRecord_.JUDGE_ROLE_TYPE_ID)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, true)).thenReturn(predicate);
        when(criteriaBuilder.equal(attributePath, true)).thenReturn(predicate);

        final SittingRecordSearchRequest recordSearchRequest = SittingRecordSearchRequest.builder()
            .offset(5)
            .pageSize(10)
            .regionId(SittingRecord_.REGION_ID)
            .epimmsId(SittingRecord_.EPIMMS_ID)
            .dateOrder(DateOrder.ASCENDING)
            .dateRangeFrom(LocalDate.now().minusDays(2))
            .dateRangeTo(LocalDate.now())
            .personalCode(SittingRecord_.PERSONAL_CODE)
            .judgeRoleTypeId(SittingRecord_.JUDGE_ROLE_TYPE_ID)
            .statusId(RECORDED)
            .duration(Duration.FULL_DAY)
            .build();

        int totalRecords = sittingRecordRepositorySearch.totalRecords(recordSearchRequest, SSCS);

        assertThat(totalRecords).isEqualTo(10);

        verify(entityManager).getCriteriaBuilder();
        verify(entityManager).createQuery(countCriteriaQuery);

        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SSCS));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.REGION_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.EPIMMS_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.PERSONAL_CODE));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(SittingRecord_.JUDGE_ROLE_TYPE_ID));
        verify(criteriaBuilder).equal(isA(SingularAttributePath.class), eq(RECORDED));
        verify(criteriaBuilder, times(2)).equal(isA(SingularAttributePath.class), eq(true));
        verify(criteriaBuilder).between(any(), eq(LocalDate.now().minusDays(2)), eq(LocalDate.now()));
    }

    private void setUpMock() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(SittingRecord.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(SittingRecord.class)).thenReturn(sittingRecord);

        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(PAGE_SIZE)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(OFF_SET)).thenReturn(typedQuery);
    }

}
