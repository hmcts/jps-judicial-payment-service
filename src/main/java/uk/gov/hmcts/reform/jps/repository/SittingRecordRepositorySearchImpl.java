package uk.gov.hmcts.reform.jps.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecord_;
import uk.gov.hmcts.reform.jps.domain.StatusHistory_;
import uk.gov.hmcts.reform.jps.model.DateOrder;
import uk.gov.hmcts.reform.jps.model.Duration;
import uk.gov.hmcts.reform.jps.model.RecordSubmitFields;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.in.SubmitSittingRecordRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.CONTRACT_TYPE_ID;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.EPIMMS_ID;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.HMCTS_SERVICE_ID;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.ID;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.JUDGE_ROLE_TYPE_ID;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.PERSONAL_CODE;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.REGION_ID;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.SITTING_DATE;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.STATUS_HISTORIES;
import static uk.gov.hmcts.reform.jps.domain.StatusHistory_.CHANGED_BY_USER_ID;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.FULL_DAY;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;

public class SittingRecordRepositorySearchImpl implements SittingRecordRepositorySearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(SittingRecordRepositorySearchImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    public SittingRecordRepositorySearchImpl() {

    }

    @Override
    public Stream<SittingRecord> find(SittingRecordSearchRequest recordSearchRequest, String hmctsServiceCode,
                                      LocalDate serviceOnboardedDate, List<String> medicalJohIds) {
        try {
            // create the outer query
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<SittingRecord> cq = cb.createQuery(SittingRecord.class);
            Root<SittingRecord> root = cq.from(SittingRecord.class);

            updateCriteriaQuery(
                recordSearchRequest,
                hmctsServiceCode,
                serviceOnboardedDate,
                medicalJohIds,
                cb,
                cq,
                root,
                predicates -> {
                    if (recordSearchRequest.getDateOrder().equals(DateOrder.ASCENDING)) {
                        LOGGER.debug("DateOrder: {}", DateOrder.ASCENDING);
                        cq.orderBy(cb.asc(root.get(SittingRecord_.SITTING_DATE)));
                    } else {
                        LOGGER.debug("DateOrder: {}", DateOrder.DESCENDING);
                        cq.orderBy(cb.desc(root.get(SittingRecord_.SITTING_DATE)));
                    }
                }
            );
            return entityManager.createQuery(cq)
                .setMaxResults(recordSearchRequest.getPageSize())
                .setFirstResult(recordSearchRequest.getOffset())
                .getResultStream();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public long totalRecords(SittingRecordSearchRequest recordSearchRequest,
                            String hmctsServiceCode, LocalDate serviceOnboardedDate, List<String> medicalJohIds) {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<SittingRecord> sittingRecord = criteriaQuery.from(SittingRecord.class);

            criteriaQuery.select(criteriaBuilder.count(sittingRecord.get(SittingRecord_.ID)));

            updateCriteriaQuery(
                recordSearchRequest,
                hmctsServiceCode,
                serviceOnboardedDate,
                medicalJohIds,
                criteriaBuilder,
                criteriaQuery,
                sittingRecord,
                predicates -> {
                }
            );

            return entityManager.createQuery(criteriaQuery).getResultStream()
                .findAny()
                .orElse(0L);
        } finally {
            entityManager.close();
        }
    }

    public void addJoinCriteria(SittingRecordSearchRequest recordSearchRequest,
                                CriteriaBuilder criteriaBuilder,
                                List<Predicate> predicates,
                                Root<SittingRecord> sittingRecord) {

        if (Objects.nonNull(recordSearchRequest.getCreatedByUserId())
            && !recordSearchRequest.getCreatedByUserId().isEmpty()) {
            Join<Object, Object> joinStatusHistory =
                sittingRecord.join(SittingRecord_.STATUS_HISTORIES, JoinType.INNER);
            Predicate predicateUserId = criteriaBuilder.equal(joinStatusHistory.get(
                StatusHistory_.CHANGED_BY_USER_ID), recordSearchRequest.getCreatedByUserId());
            Predicate predicateStatusRecorded = criteriaBuilder.equal(joinStatusHistory.get(
                StatusHistory_.STATUS_ID), StatusId.RECORDED);
            predicates.add(criteriaBuilder.and(predicateUserId, predicateStatusRecorded));
        }
    }

    public Predicate getStatusPredicate(Root<SittingRecord> sittingRecord,
                                        CriteriaBuilder criteriaBuilder,
                                        StatusId statusId) {
        return criteriaBuilder.equal(sittingRecord.get(SittingRecord_.STATUS_ID),
                                                          statusId);
    }

    public Predicate getSittingDateRangePredicate(Root<SittingRecord> sittingRecord,
                                        CriteriaBuilder criteriaBuilder,
                                        LocalDate dateFrom, LocalDate dateTo) {
        LOGGER.debug("predicateDateRange: sittingDate between {} and {}", dateFrom, dateTo);
        return criteriaBuilder.between(
            sittingRecord.get(SittingRecord_.SITTING_DATE),
            dateFrom,
            dateTo);
    }

    public List<Predicate> getRecordedDateRangePredicates(Root<SittingRecord> sittingRecord,
                                                           CriteriaBuilder criteriaBuilder,
                                                           LocalDate serviceOnboardedDate,
                                                           LocalDate dateTo) {

        List<Predicate> predicatesRecordedDateRange = new ArrayList<>();
        predicatesRecordedDateRange.add(getStatusPredicate(sittingRecord,
                                                          criteriaBuilder,
                                                          StatusId.RECORDED));

        LOGGER.debug("predicatePublishedDateRange: sittingDate between serviceOnboardedDate, dateTo");
        predicatesRecordedDateRange.add(getSittingDateRangePredicate(sittingRecord, criteriaBuilder,
                                                                    serviceOnboardedDate,
                                           (dateTo.isBefore(serviceOnboardedDate) ? serviceOnboardedDate : dateTo)));

        return predicatesRecordedDateRange;
    }

    @Override
    public List<RecordSubmitFields> findRecordsToSubmit(SubmitSittingRecordRequest recordSearchRequest,
                                                        String hmctsServiceCode) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<RecordSubmitFields> criteriaQuery = criteriaBuilder.createQuery(RecordSubmitFields.class);
        Root<SittingRecord> sittingRecord = criteriaQuery.from(SittingRecord.class);

        criteriaQuery.multiselect(
            sittingRecord.get(ID),
            sittingRecord.get(CONTRACT_TYPE_ID),
            sittingRecord.get(PERSONAL_CODE),
            sittingRecord.get(SITTING_DATE));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(sittingRecord.get(HMCTS_SERVICE_ID),
                                             hmctsServiceCode));
        predicates.add(criteriaBuilder.equal(sittingRecord.get(REGION_ID), recordSearchRequest.getRegionId()));
        predicates.add(criteriaBuilder.between(sittingRecord.get(SITTING_DATE),
                                               recordSearchRequest.getDateRangeFrom(),
                                               recordSearchRequest.getDateRangeTo()));

        if (Objects.nonNull(recordSearchRequest.getCreatedByUserId())) {
            Join<Object, Object> statusHistories = sittingRecord.join(STATUS_HISTORIES, JoinType.INNER);
            predicates.add(criteriaBuilder.equal(statusHistories.get(CHANGED_BY_USER_ID),
                                                 recordSearchRequest.getCreatedByUserId()));
            predicates.add(criteriaBuilder.equal(statusHistories.get(StatusHistory_.STATUS_ID),
                                                 StatusId.RECORDED));
        }
        predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.STATUS_ID),
                                             StatusId.RECORDED));

        Optional.ofNullable(recordSearchRequest.getEpimmsId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get(EPIMMS_ID), value)));

        Optional.ofNullable(recordSearchRequest.getPersonalCode())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get(PERSONAL_CODE), value)));

        Optional.ofNullable(recordSearchRequest.getJudgeRoleTypeId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get(JUDGE_ROLE_TYPE_ID), value)));

        setDurationPredicates(criteriaBuilder, sittingRecord, predicates, recordSearchRequest.getDuration());

        Predicate[] predicatesArray = new Predicate[predicates.size()];
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicatesArray)));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }


    private <T> void updateCriteriaQuery(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode,
        LocalDate serviceOnboardedDate,
        List<String> medicalJohIds,
        CriteriaBuilder criteriaBuilder,
        CriteriaQuery<T> criteriaQuery,
        Root<SittingRecord> sittingRecord,
        Consumer<List<Predicate>> predicateConsumer) {

        final List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.notEqual(sittingRecord.get(SittingRecord_.STATUS_ID), StatusId.DELETED));

        predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.HMCTS_SERVICE_ID), hmctsServiceCode));

        Optional.ofNullable(recordSearchRequest.getRegionId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(
                sittingRecord.get(SittingRecord_.REGION_ID), value)));

        Optional.ofNullable(recordSearchRequest.getEpimmsId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(
                sittingRecord.get(SittingRecord_.EPIMMS_ID), value)));

        Optional.ofNullable(recordSearchRequest.getPersonalCode())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(
                sittingRecord.get(SittingRecord_.PERSONAL_CODE), value)));

        Optional.ofNullable(recordSearchRequest.getJudgeRoleTypeId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(
                sittingRecord.get(SittingRecord_.JUDGE_ROLE_TYPE_ID), value)));

        Optional.ofNullable(recordSearchRequest.getStatusId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(
                sittingRecord.get(SittingRecord_.STATUS_ID), value)));

        Optional<Duration> duration = Optional.ofNullable(recordSearchRequest.getDuration());

        if (duration.isPresent()) {
            if (duration.get().equals(FULL_DAY)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.AM), true));
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.PM), true));
            } else if (duration.get().equals(AM)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.AM), true));
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.PM), false));
            } else if (duration.get().equals(PM)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.PM), true));
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.AM), false));
            }
        }

        addJoinCriteria(recordSearchRequest, criteriaBuilder, predicates, sittingRecord);

        if (Boolean.TRUE.equals(recordSearchRequest.getMedicalMembersOnly())) {
            selectMedicalMembers(sittingRecord, criteriaBuilder, medicalJohIds, predicates);
        }

        List<Predicate> predicatesClosed = getClosedDateRangePredicates(sittingRecord, criteriaBuilder,
                                                                        recordSearchRequest.getDateRangeFrom());

        List<Predicate> predicatesPublished = getPublishedDateRangePredicates(sittingRecord, criteriaBuilder,
                                                                              recordSearchRequest.getDateRangeFrom());

        List<Predicate> predicatesSubmitted = getSubmittedDateRangePredicates(sittingRecord, criteriaBuilder,
                                                                              serviceOnboardedDate,
                                                                              recordSearchRequest.getDateRangeTo());

        List<Predicate> predicatesRecorded = getRecordedDateRangePredicates(sittingRecord, criteriaBuilder,
                                                                            serviceOnboardedDate,
                                                                            recordSearchRequest.getDateRangeTo());

        // Combine the OR conditions using criteriaBuilder.or
        List<Predicate> finalPredicates = predicates;
        finalPredicates.addAll(predicatesClosed);
        finalPredicates.addAll(predicatesPublished);
        finalPredicates.addAll(predicatesRecorded);
        finalPredicates.addAll(predicatesSubmitted);

        Predicate finalPredicate = buildFinalPredicate(predicatesClosed, predicatesPublished, predicatesRecorded,
                                                     predicatesSubmitted, criteriaBuilder, predicates);

        predicateConsumer.accept(finalPredicates);

        criteriaQuery.where(finalPredicate);
    }

    protected void selectMedicalMembers(Root<SittingRecord> sittingRecord,
                                             CriteriaBuilder criteriaBuilder,
                                             List<String> medicalJohIds,
                                             List<Predicate> predicates) {
        Expression<String> attributeToCheck = sittingRecord.get(JUDGE_ROLE_TYPE_ID);
        Predicate inPredicate = attributeToCheck.in(medicalJohIds);

        predicates.add(inPredicate);
    }

    protected List<Predicate> getClosedDateRangePredicates(Root<SittingRecord> sittingRecord,
                                                        CriteriaBuilder criteriaBuilder,
                                                        LocalDate dateFrom) {

        List<Predicate> predicatesClosedDateRange = new ArrayList<>();
        predicatesClosedDateRange.add(getStatusPredicate(sittingRecord,
                                                         criteriaBuilder,
                                                         StatusId.CLOSED));

        LOGGER.debug("predicateClosedDateRange: sittingDate between dateFrom, now");
        predicatesClosedDateRange.add(getSittingDateRangePredicate(sittingRecord, criteriaBuilder,
                                                                   dateFrom, LocalDate.now()));

        return predicatesClosedDateRange;
    }

    protected List<Predicate> getPublishedDateRangePredicates(Root<SittingRecord> sittingRecord,
                                                           CriteriaBuilder criteriaBuilder,
                                                           LocalDate dateFrom) {

        List<Predicate> predicatesPublishedDateRange = new ArrayList<>();
        predicatesPublishedDateRange.add(getStatusPredicate(sittingRecord,
                                                            criteriaBuilder,
                                                            StatusId.PUBLISHED));
        LOGGER.debug("predicatePublishedDateRange: sittingDate between dateFrom, now");
        predicatesPublishedDateRange.add(getSittingDateRangePredicate(sittingRecord,
                                                                      criteriaBuilder,
                                                                      dateFrom, LocalDate.now()));

        return predicatesPublishedDateRange;
    }

    protected List<Predicate> getSubmittedDateRangePredicates(Root<SittingRecord> sittingRecord,
                                                           CriteriaBuilder criteriaBuilder,
                                                           LocalDate serviceOnboardedDate,
                                                           LocalDate dateTo) {

        List<Predicate> predicatesSubmittedDateRange = new ArrayList<>();
        predicatesSubmittedDateRange.add(getStatusPredicate(sittingRecord,
                                                            criteriaBuilder,
                                                            StatusId.SUBMITTED));

        LOGGER.debug("predicatePublishedDateRange: sittingDate between serviceOnboardedDate, dateTo");
        LocalDate paramDateTo = dateTo.isBefore(serviceOnboardedDate) ? serviceOnboardedDate : dateTo;
        predicatesSubmittedDateRange.add(getSittingDateRangePredicate(sittingRecord, criteriaBuilder,
                                                                      serviceOnboardedDate, paramDateTo));

        return predicatesSubmittedDateRange;
    }

    protected Predicate buildFinalPredicate(List<Predicate> predicatesClosed, List<Predicate> predicatesPublished,
                                              List<Predicate> predicatesRecorded, List<Predicate> predicatesSubmitted,
                                              CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {

        Predicate[] predicatesArray = predicates.toArray(new Predicate[predicates.size()]);
        Predicate[] predicatesClosedArray = predicatesClosed.toArray(new Predicate[predicatesClosed.size()]);
        Predicate[] predicatesPublishedArray = predicatesPublished.toArray(new Predicate[predicatesPublished.size()]);
        Predicate[] predicatesRecordedArray = predicatesRecorded.toArray(new Predicate[predicatesRecorded.size()]);
        Predicate[] predicatesSubmittedArray = predicatesSubmitted.toArray(new Predicate[predicatesSubmitted.size()]);

        Predicate andPredicate = criteriaBuilder.and(predicatesArray);
        Predicate orPredicateClosed = criteriaBuilder.and(predicatesClosedArray);
        Predicate orPredicatePublished = criteriaBuilder.and(predicatesPublishedArray);
        Predicate orPredicateRecorded = criteriaBuilder.and(predicatesRecordedArray);
        Predicate orPredicateSubmitted = criteriaBuilder.and(predicatesSubmittedArray);
        Predicate orPredicate = criteriaBuilder.or(orPredicateClosed, orPredicatePublished, orPredicateRecorded,
                                                   orPredicateSubmitted);

        // Combine the OR conditions using criteriaBuilder.or
        return criteriaBuilder.and(orPredicate, andPredicate);
    }

    private <V> void setDurationPredicates(CriteriaBuilder criteriaBuilder,
                                                  Root<V> sittingRecord,
                                                  List<Predicate> predicates,
                                                  Duration duration) {
        if (Objects.nonNull(duration)) {
            if (duration.equals(FULL_DAY)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.AM), true));
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.PM), true));
            } else if (duration.equals(AM)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.AM), true));
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.PM), false));
            } else if (duration.equals(PM)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.PM), true));
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.AM), false));
            }
        }
    }

}
