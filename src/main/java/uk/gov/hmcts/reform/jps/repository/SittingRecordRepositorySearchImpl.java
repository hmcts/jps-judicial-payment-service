package uk.gov.hmcts.reform.jps.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SittingRecordRepositorySearchImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    private void updateCriteriaQuery(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode,
        CriteriaBuilder criteriaBuilder,
        CriteriaQuery criteriaQuery,
        Root<SittingRecord> sittingRecord,
        Consumer<List<Predicate>> predicateConsumer) {

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.HMCTS_SERVICE_ID), hmctsServiceCode));
        predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.REGION_ID),
                                             recordSearchRequest.getRegionId()));
        predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.EPIMS_ID),
                                             recordSearchRequest.getEpimsId()));
        predicates.add(criteriaBuilder.between(sittingRecord.get(SittingRecord_.SITTING_DATE),
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
                sittingRecord.get(SittingRecord_.STATUS_ID), value.name())));

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

        addJoinCriteria(recordSearchRequest, criteriaBuilder, criteriaQuery,sittingRecord);

        predicateConsumer.accept(predicates);

        Predicate[] predicatesArray = new Predicate[predicates.size()];
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicatesArray)));
    }

    @Override
    public List<SittingRecord> find(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {
        LOGGER.debug("find(?): ...");

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
                    LOGGER.debug("DateOrder: {}", DateOrder.ASCENDING);
                    cq.orderBy(cb.asc(root.get(SittingRecord_.SITTING_DATE)));
                } else {
                    LOGGER.debug("DateOrder: {}", DateOrder.DESCENDING);
                    cq.orderBy(cb.desc(root.get(SittingRecord_.SITTING_DATE)));
                }
            }
        );

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

        return entityManager.createQuery(criteriaQuery).getSingleResult().intValue();
    }

    @SuppressWarnings("unchecked")
    public void addJoinCriteria(SittingRecordSearchRequest recordSearchRequest,
                                          CriteriaBuilder criteriaBuilder,
                                          CriteriaQuery criteriaQuery,
                                          Root<SittingRecord> sittingRecord) {

        if (null != recordSearchRequest.getCreatedByUserId() && !recordSearchRequest.getCreatedByUserId().isEmpty()) {
            Join<Object, Object> joinStatusHistory =
                sittingRecord.join(SittingRecord_.STATUS_HISTORIES, JoinType.INNER);

            criteriaQuery.groupBy(sittingRecord.get(SittingRecord_.ID), joinStatusHistory.get(StatusHistory_.ID),
                                  joinStatusHistory.get(StatusHistory_.CHANGE_BY_USER_ID))
                .having(criteriaBuilder.equal(criteriaBuilder.min(joinStatusHistory.get(StatusHistory_.ID)),
                                              joinStatusHistory.get(StatusHistory_.ID)),
                        criteriaBuilder.equal(joinStatusHistory.get(StatusHistory_.CHANGE_BY_USER_ID),
                                              recordSearchRequest.getCreatedByUserId()));

            LOGGER.debug("Group By sittingRecord.Id, statusHistory.Id and selected created by user");

        }

    }

}
