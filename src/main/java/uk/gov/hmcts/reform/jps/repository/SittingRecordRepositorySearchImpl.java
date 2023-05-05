package uk.gov.hmcts.reform.jps.repository;

import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.model.DateOrder;
import uk.gov.hmcts.reform.jps.model.Duration;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.FULL_DAY;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;

public class SittingRecordRepositorySearchImpl implements SittingRecordRepositorySearch {

    @PersistenceContext
    private EntityManager entityManager;

    private <T,V> void updateCriteriaQuery(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode,
        CriteriaBuilder criteriaBuilder,
        CriteriaQuery<T> criteriaQuery,
        Root<V> sittingRecord,
        Consumer<List<Predicate>> predicateConsumer,
        boolean isCountQuery) {

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(sittingRecord.get("hmctsServiceId"), hmctsServiceCode));
        predicates.add(criteriaBuilder.equal(sittingRecord.get("regionId"), recordSearchRequest.getRegionId()));
        predicates.add(criteriaBuilder.equal(sittingRecord.get("epimsId"), recordSearchRequest.getEpimsId()));
        predicates.add(criteriaBuilder.between(sittingRecord.get("sittingDate"),
                                               recordSearchRequest.getDateRangeFrom(),
                                               recordSearchRequest.getDateRangeTo()));

        Optional.ofNullable(recordSearchRequest.getPersonalCode())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get("personalCode"), value)));

        Optional.ofNullable(recordSearchRequest.getJudgeRoleTypeId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get("judgeRoleTypeId"), value)));

        Optional.ofNullable(recordSearchRequest.getStatusId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get("statusId"), value.name())));

        predicateConsumer.accept(predicates);

        Optional<Duration> duration = Optional.ofNullable(recordSearchRequest.getDuration());

        if (duration.isPresent()) {
            if (duration.get().equals(FULL_DAY)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get("am"), true));
                predicates.add(criteriaBuilder.equal(sittingRecord.get("pm"), true));
            } else if (duration.get().equals(AM)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get("am"), true));
            } else if (duration.get().equals(PM)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get("pm"), true));
            }
        }

        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));

        if (!isCountQuery) {
            if (recordSearchRequest.getDateOrder().equals(DateOrder.ASCENDING)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(sittingRecord.get("createdDateTime")));
            } else {
                criteriaQuery.orderBy(criteriaBuilder.desc(sittingRecord.get("createdDateTime")));
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
                            predicates ->
                                Optional.ofNullable(recordSearchRequest.getCreatedByUserId())
                                    .ifPresent(value ->
                                       predicates.add(criteriaBuilder.equal(sittingRecord.get("createdByUserId"),
                                            value))),
                false);

        TypedQuery<SittingRecord> typedQuery = entityManager.createQuery(criteriaQuery)
            .setMaxResults(recordSearchRequest.getPageSize())
            .setFirstResult(recordSearchRequest.getOffset());

        return typedQuery.getResultList();
    }

    @Override
    public List<SittingRecord> findByUser(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode,
        String userId) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SittingRecord> criteriaQuery = criteriaBuilder.createQuery(SittingRecord.class);
        Root<SittingRecord> sittingRecord = criteriaQuery.from(SittingRecord.class);
        criteriaQuery.select(sittingRecord);

        updateCriteriaQuery(recordSearchRequest,
                            hmctsServiceCode,
                            criteriaBuilder,
                            criteriaQuery,
                            sittingRecord,
                            predicates ->
                                predicates.add(criteriaBuilder.equal(sittingRecord.get("createdByUserId"), userId)),
                            false);

        TypedQuery<SittingRecord> typedQuery = entityManager.createQuery(criteriaQuery)
            .setMaxResults(recordSearchRequest.getPageSize())
            .setFirstResult(recordSearchRequest.getOffset());

        return typedQuery.getResultList();
    }

    @Override
    public List<SittingRecord> findByIgnoreUserId(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode,
        String userId) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SittingRecord> criteriaQuery = criteriaBuilder.createQuery(SittingRecord.class);
        Root<SittingRecord> sittingRecord = criteriaQuery.from(SittingRecord.class);

        criteriaQuery.select(sittingRecord);

        updateCriteriaQuery(recordSearchRequest,
                            hmctsServiceCode,
                            criteriaBuilder,
                            criteriaQuery,
                            sittingRecord,
                            predicates ->
                                Optional.ofNullable(userId)
                                    .ifPresent(value ->
                                       predicates.add(criteriaBuilder.notEqual(sittingRecord.get("createdByUserId"),
                                           value))),
                false);


        TypedQuery<SittingRecord> query = entityManager.createQuery(criteriaQuery);

        return query.getResultList();
    }

    @Override
    public int recordCountByUser(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode,
        String userId) {

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
                                predicates.add(criteriaBuilder.equal(sittingRecord.get("createdByUserId"), userId)),
                true);


        return entityManager.createQuery(criteriaQuery).getSingleResult().intValue();
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
                                            value))),
                true);

        return entityManager.createQuery(criteriaQuery).getSingleResult().intValue();
    }
}
