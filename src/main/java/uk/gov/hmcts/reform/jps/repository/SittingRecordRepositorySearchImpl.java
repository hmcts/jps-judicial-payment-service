package uk.gov.hmcts.reform.jps.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecord_;
import uk.gov.hmcts.reform.jps.domain.StatusHistory_;
import uk.gov.hmcts.reform.jps.model.DateOrder;
import uk.gov.hmcts.reform.jps.model.Duration;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.in.SubmitSittingRecordRequest;

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
import static uk.gov.hmcts.reform.jps.domain.StatusHistory_.CHANGED_BY_USER_ID;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.FULL_DAY;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

public class SittingRecordRepositorySearchImpl implements SittingRecordRepositorySearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(SittingRecordRepositorySearchImpl.class);

    @PersistenceContext
    private EntityManager entityManager;


    private <T> void updateCriteriaQuery(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode,
        CriteriaBuilder criteriaBuilder,
        CriteriaQuery<T> criteriaQuery,
        Root<SittingRecord> sittingRecord,
        Consumer<List<Predicate>> predicateConsumer) {

        final List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.HMCTS_SERVICE_ID), hmctsServiceCode));

        predicates.add(criteriaBuilder.between(sittingRecord.get(SittingRecord_.SITTING_DATE),
                                               recordSearchRequest.getDateRangeFrom(),
                                               recordSearchRequest.getDateRangeTo()));

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

        addJoinCriteria(recordSearchRequest, criteriaBuilder, predicates,sittingRecord);

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
            } else if (duration.equals(AM)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.AM), true));
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.PM), false));
            } else if (duration.equals(PM)) {
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.PM), true));
                predicates.add(criteriaBuilder.equal(sittingRecord.get(SittingRecord_.AM), false));
            }
        }
    }

    @Override
    public Stream<SittingRecord> find(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {
        try {
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
                            String hmctsServiceCode) {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<SittingRecord> sittingRecord = criteriaQuery.from(SittingRecord.class);

            criteriaQuery.select(criteriaBuilder.count(sittingRecord.get(SittingRecord_.ID)));

            updateCriteriaQuery(
                recordSearchRequest,
                hmctsServiceCode,
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
                                          final List<Predicate> predicate,
                                          Root<SittingRecord> sittingRecord) {

        if (Objects.nonNull(recordSearchRequest.getCreatedByUserId())
            && !recordSearchRequest.getCreatedByUserId().isEmpty()) {
            Join<Object, Object> joinStatusHistory =
                sittingRecord.join(SittingRecord_.STATUS_HISTORIES, JoinType.INNER);
            predicate.add(criteriaBuilder.equal(joinStatusHistory.get(StatusHistory_.CHANGED_BY_USER_ID),
                                                recordSearchRequest.getCreatedByUserId()));
            predicate.add(criteriaBuilder.equal(joinStatusHistory.get(StatusHistory_.STATUS_ID),
                                                StatusId.RECORDED));
        }

    }


    @Override
    public Stream<Long> findRecordsToSubmit(SubmitSittingRecordRequest recordSearchRequest,
                                                   String hmctsServiceCode) {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<SittingRecord> sittingRecord = criteriaQuery.from(SittingRecord.class);

            criteriaQuery.select(sittingRecord.get(ID));

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(
                sittingRecord.get(HMCTS_SERVICE_ID),
                hmctsServiceCode
            ));
            predicates.add(criteriaBuilder.equal(sittingRecord.get(REGION_ID), recordSearchRequest.getRegionId()));
            predicates.add(criteriaBuilder.between(
                sittingRecord.get(SITTING_DATE),
                recordSearchRequest.getDateRangeFrom(),
                recordSearchRequest.getDateRangeTo()
            ));

            if (Objects.nonNull(recordSearchRequest.getCreatedByUserId())) {
                Join<Object, Object> statusHistories = sittingRecord.join(STATUS_HISTORIES, JoinType.INNER);
                predicates.add(criteriaBuilder.equal(
                    statusHistories.get(CHANGED_BY_USER_ID),
                    recordSearchRequest.getCreatedByUserId()
                ));
                predicates.add(criteriaBuilder.equal(
                    statusHistories.get(StatusHistory_.STATUS_ID),
                    RECORDED
                ));
            }
            predicates.add(criteriaBuilder.equal(
                sittingRecord.get(SittingRecord_.STATUS_ID),
                RECORDED
            ));

            Optional.ofNullable(recordSearchRequest.getEpimmsId())
                .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get(EPIMMS_ID), value)));

            Optional.ofNullable(recordSearchRequest.getPersonalCode())
                .ifPresent(value -> predicates.add(criteriaBuilder.equal(sittingRecord.get(PERSONAL_CODE), value)));

            Optional.ofNullable(recordSearchRequest.getJudgeRoleTypeId())
                .ifPresent(value -> predicates.add(
                    criteriaBuilder.equal(sittingRecord.get(JUDGE_ROLE_TYPE_ID), value))
                );

            setDurationPredicates(criteriaBuilder, sittingRecord, predicates, recordSearchRequest.getDuration());

            Predicate[] predicatesArray = new Predicate[predicates.size()];
            criteriaQuery.where(criteriaBuilder.and(predicates.toArray(predicatesArray)));

            return entityManager.createQuery(criteriaQuery)
                .getResultStream();
        } finally {
            entityManager.close();
        }
    }
}
