package uk.gov.hmcts.reform.jps.repository;

import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecord_;
import uk.gov.hmcts.reform.jps.domain.StatusHistory_;
import uk.gov.hmcts.reform.jps.model.DateOrder;
import uk.gov.hmcts.reform.jps.model.Duration;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.in.SubmitSittingRecordRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.EPIMMS_ID;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.HMCTS_SERVICE_ID;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.ID;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.JUDGE_ROLE_TYPE_ID;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.PERSONAL_CODE;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.REGION_ID;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.SITTING_DATE;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.STATUS_HISTORIES;
import static uk.gov.hmcts.reform.jps.domain.SittingRecord_.STATUS_ID;
import static uk.gov.hmcts.reform.jps.domain.StatusHistory_.CHANGE_BY_USER_ID;
import static uk.gov.hmcts.reform.jps.model.Duration.FULL_DAY;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

public class SittingRecordRepositorySearchImpl implements SittingRecordRepositorySearch {

    @PersistenceContext
    private EntityManager entityManager;

    private <T,V> void updateCriteriaQuery(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode,
        CriteriaBuilder criteriaBuilder,
        CriteriaQuery<T> criteriaQuery,
        Root<V> sittingRecord,
        Consumer<List<Predicate>> predicateConsumer) {

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(sittingRecord.get(HMCTS_SERVICE_ID), hmctsServiceCode));
        predicates.add(criteriaBuilder.equal(sittingRecord.get(REGION_ID), recordSearchRequest.getRegionId()));
        predicates.add(criteriaBuilder.equal(sittingRecord.get(EPIMMS_ID), recordSearchRequest.getEpimmsId()));
        predicates.add(criteriaBuilder.between(sittingRecord.get(SITTING_DATE),
                                               recordSearchRequest.getDateRangeFrom(),
                                               recordSearchRequest.getDateRangeTo()));

        Optional.ofNullable(recordSearchRequest.getPersonalCode())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get(PERSONAL_CODE), value)));

        Optional.ofNullable(recordSearchRequest.getJudgeRoleTypeId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get(JUDGE_ROLE_TYPE_ID), value)));

        Optional.ofNullable(recordSearchRequest.getStatusId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get(STATUS_ID), value)));

        setDurationPredicates(criteriaBuilder, sittingRecord, predicates, recordSearchRequest.getDuration());

        predicateConsumer.accept(predicates);

        Predicate[] predicatesArray = new Predicate[predicates.size()];
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicatesArray)));
    }

    private <V> void setDurationPredicates(CriteriaBuilder criteriaBuilder,
                                                  Root<V> sittingRecord,
                                                  List<Predicate> predicates,
                                                  Duration duration) {
        if (Objects.nonNull(duration)) {
            if (duration.equals(FULL_DAY)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.AM), true));
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.PM), true));
            } else if (duration.equals(Duration.AM)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.AM), true));
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.PM), false));
            } else if (duration.equals(Duration.PM)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.PM), true));
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.AM), false));
            }
        }
    }

    @Override
    public List<SittingRecord> find(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SittingRecord> criteriaQuery = criteriaBuilder.createQuery(SittingRecord.class);
        Root<SittingRecord> sittingRecord = criteriaQuery.from(SittingRecord.class);
        criteriaQuery.select(sittingRecord);

        updateCriteriaQuery(recordSearchRequest,
                            hmctsServiceCode,
                            criteriaBuilder,
                            criteriaQuery,
                            sittingRecord,
                            predicates -> {
                                Optional.ofNullable(recordSearchRequest.getCreatedByUserId())
                                    .ifPresent(value ->
                                                   predicates.add(criteriaBuilder.equal(
                                                       sittingRecord.get("createdByUserId"),
                                                       value
                                                   )));

                                if (recordSearchRequest.getDateOrder().equals(DateOrder.ASCENDING)) {
                                    criteriaQuery.orderBy(criteriaBuilder.asc(sittingRecord.get("sittingDate")));
                                } else {
                                    criteriaQuery.orderBy(criteriaBuilder.desc(sittingRecord.get("sittingDate")));
                                }
                            }
                            );

        TypedQuery<SittingRecord> typedQuery = entityManager.createQuery(criteriaQuery)
            .setMaxResults(recordSearchRequest.getPageSize())
            .setFirstResult(recordSearchRequest.getOffset());

        return typedQuery.getResultList();
    }

    @Override
    public int totalRecords(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<SittingRecord> sittingRecord = criteriaQuery.from(SittingRecord.class);

        criteriaQuery.select(criteriaBuilder.count(sittingRecord));

        updateCriteriaQuery(recordSearchRequest,
                            hmctsServiceCode,
                            criteriaBuilder,
                            criteriaQuery,
                            sittingRecord,
                            predicates ->
                                Optional.ofNullable(recordSearchRequest.getCreatedByUserId())
                                    .ifPresent(value ->
                                       predicates.add(criteriaBuilder.equal(sittingRecord.get("createdByUserId"),
                                            value)))
        );

        return entityManager.createQuery(criteriaQuery).getSingleResult().intValue();
    }

    @Override
    public List<Long> findRecordsToSubmit(SubmitSittingRecordRequest recordSearchRequest,
                                                   String hmctsServiceCode) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<SittingRecord> sittingRecord = criteriaQuery.from(SittingRecord.class);

        criteriaQuery.select(sittingRecord.get(ID));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(sittingRecord.get(HMCTS_SERVICE_ID),
                                             hmctsServiceCode));
        predicates.add(criteriaBuilder.equal(sittingRecord.get(REGION_ID), recordSearchRequest.getRegionId()));
        predicates.add(criteriaBuilder.between(sittingRecord.get(SITTING_DATE),
                                               recordSearchRequest.getDateRangeFrom(),
                                               recordSearchRequest.getDateRangeTo()));

        if (Objects.nonNull(recordSearchRequest.getCreatedByUserId())) {
            Join<Object, Object> statusHistories = sittingRecord.join(STATUS_HISTORIES, JoinType.INNER);
            predicates.add(criteriaBuilder.equal(statusHistories.get(CHANGE_BY_USER_ID),
                                                 recordSearchRequest.getCreatedByUserId()));
            predicates.add(criteriaBuilder.equal(statusHistories.get(StatusHistory_.STATUS_ID),
                                                 RECORDED));
        }
        predicates.add(criteriaBuilder.equal(sittingRecord.get(STATUS_ID),
                                             RECORDED));

        Optional.ofNullable(recordSearchRequest.getEpimmsId())
                .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get(EPIMMS_ID), value)));

        Optional.ofNullable(recordSearchRequest.getPersonalCode())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get(PERSONAL_CODE), value)));

        Optional.ofNullable(recordSearchRequest.getJudgeRoleTypeId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get(JUDGE_ROLE_TYPE_ID), value)));

        setDurationPredicates(criteriaBuilder, sittingRecord, predicates, recordSearchRequest.getDuration());

        Predicate[] predicatesArray = new Predicate[predicates.size()];
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicatesArray)));

        TypedQuery<Long> typedQuery = entityManager.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }


}
