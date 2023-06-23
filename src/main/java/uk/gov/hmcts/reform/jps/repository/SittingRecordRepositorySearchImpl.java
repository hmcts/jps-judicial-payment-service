package uk.gov.hmcts.reform.jps.repository;

import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecord_;
import uk.gov.hmcts.reform.jps.domain.StatusHistory_;
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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.FULL_DAY;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;

public class SittingRecordRepositorySearchImpl implements SittingRecordRepositorySearch {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String SITTING_DATE = "sittingDate";

    private <T,V> void updateCriteriaQuery(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode,
        CriteriaBuilder criteriaBuilder,
        CriteriaQuery<T> criteriaQuery,
        Root<V> sittingRecord,
        Consumer<List<Predicate>> predicateConsumer) {

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(sittingRecord.get("hmctsServiceId"), hmctsServiceCode));
        predicates.add(criteriaBuilder.equal(sittingRecord.get("regionId"), recordSearchRequest.getRegionId()));
        predicates.add(criteriaBuilder.equal(sittingRecord.get("epimsId"), recordSearchRequest.getEpimsId()));
        predicates.add(criteriaBuilder.between(sittingRecord.get(SITTING_DATE),
                                               recordSearchRequest.getDateRangeFrom(),
                                               recordSearchRequest.getDateRangeTo()));

        Optional.ofNullable(recordSearchRequest.getPersonalCode())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(
                sittingRecord.get(SittingRecord_.PERSONAL_CODE), value)));

        Optional.ofNullable(recordSearchRequest.getJudgeRoleTypeId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(
                sittingRecord.get(SittingRecord_.JUDGE_ROLE_TYPE_ID), value)));

        Optional.ofNullable(recordSearchRequest.getStatusId())
            .ifPresent(value -> predicates.add(criteriaBuilder.equal(
                sittingRecord.get("statusId"), value.name())));

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

        predicateConsumer.accept(predicates);

        Predicate[] predicatesArray = new Predicate[predicates.size()];
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicatesArray)));
    }

    @Override
    public List<SittingRecord> find(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {

        // create the outer query
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SittingRecord> cq = cb.createQuery(SittingRecord.class);
        Root<SittingRecord> root = cq.from(SittingRecord.class);

        updateCriteriaQuery(
            recordSearchRequest,
            hmctsServiceCode,
            cb,
            cq,
            root,
            predicates -> {
                if (recordSearchRequest.getDateOrder().equals(DateOrder.ASCENDING)) {
                    cq.orderBy(cb.asc(root.get(SITTING_DATE)));
                } else {
                    cq.orderBy(cb.desc(root.get(SITTING_DATE)));
                }
            }
        );

        checkUserIdSelection(recordSearchRequest, cb, cq, root);

        TypedQuery<SittingRecord> typedQuery = entityManager.createQuery(cq)
            .setMaxResults(recordSearchRequest.getPageSize())
            .setFirstResult(recordSearchRequest.getOffset());

        return typedQuery.getResultList();
    }

    @Override
    public int totalRecords(SittingRecordSearchRequest recordSearchRequest,
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
                            predicates -> { }
        );

        checkUserIdSelection(recordSearchRequest, criteriaBuilder, criteriaQuery, sittingRecord);

        return entityManager.createQuery(criteriaQuery).getSingleResult().intValue();
    }

    public void checkUserIdSelection(SittingRecordSearchRequest recordSearchRequest, CriteriaBuilder cb,
                                        CriteriaQuery cq, Root<SittingRecord> root) {

        if (null != recordSearchRequest.getCreatedByUserId() && !recordSearchRequest.getCreatedByUserId().isEmpty()) {
            addChangeByUserIdCriteria(recordSearchRequest.getCreatedByUserId(), cb, cq, root);
        }
    }

    @SuppressWarnings("unchecked")
    public void addChangeByUserIdCriteria(String value,
                                          CriteriaBuilder cb,
                                          CriteriaQuery cq,
                                          Root<SittingRecord> rootSR) {


        // @Query("select sh.changeByUserId from SittingRecord sr inner join StatusHistory sh
        // on sh.sittingRecord.id = sr.id where sh.id = (select min(sh2.id) from StatusHistory sh2
        // where sh2.sittingRecord.id = :id)")

        Join<Object, Object> joinStatusHistory = (Join<Object, Object>)
            rootSR.fetch(SittingRecord_.STATUS_HISTORIES, JoinType.INNER);

        cq.groupBy(rootSR.get(SittingRecord_.id), joinStatusHistory.get(StatusHistory_.ID),
                   joinStatusHistory.get(StatusHistory_.CHANGE_BY_USER_ID))
            .having(cb.equal(cb.min(joinStatusHistory.get(StatusHistory_.ID)),
                             joinStatusHistory.get(StatusHistory_.ID)),
                    cb.equal(joinStatusHistory.get(StatusHistory_.CHANGE_BY_USER_ID), value));

    }

}
