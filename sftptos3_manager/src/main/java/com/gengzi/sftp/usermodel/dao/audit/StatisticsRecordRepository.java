package com.gengzi.sftp.usermodel.dao.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatisticsRecordRepository extends JpaRepository<StatisticsRecord, Long>, JpaSpecificationExecutor<StatisticsRecord> {


    @Query("select s from StatisticsRecord s where s.startTime = :startTime and s.endTime = :endTime")
    List<StatisticsRecord> findByStartTimeAndEndTime(LocalDateTime startTime, LocalDateTime endTime);

    StatisticsRecord findTopByStatusOrderByEndTimeDesc(Byte status);

    List<StatisticsRecord> findStatisticsRecordByStartTimeBetween(@NotNull LocalDateTime startTimeAfter, @NotNull LocalDateTime startTimeBefore);

    List<StatisticsRecord> findByStartTimeBetween(@NotNull LocalDateTime startTimeAfter, @NotNull LocalDateTime startTimeBefore);
}