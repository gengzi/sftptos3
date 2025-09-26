package com.gengzi.sftp.usermodel.response;

import lombok.Data;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO for {@link com.gengzi.sftp.usermodel.dao.audit.StatisticsRecord}
 */
@Data
public class StatisticsRecordResponse implements Serializable {
    @NotNull
    Long authCountVal;
    @NotNull
    Long authSuccessVal;
    @NotNull
    Long authFailureVal;
    @NotNull
    Long downloadCountVal;
    @NotNull
    Long downloadSuccessVal;
    @NotNull
    Long downloadFailureVal;
    @NotNull
    Long uploadCountVal;
    @NotNull
    Long uploadSuccessVal;
    @NotNull
    Long uploadFailureVal;
    @NotNull
    Long delCountVal;
    @NotNull
    Long delSuccessVal;
    @NotNull
    Long delFailureVal;
}