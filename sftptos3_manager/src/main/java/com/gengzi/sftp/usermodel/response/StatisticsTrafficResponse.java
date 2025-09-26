package com.gengzi.sftp.usermodel.response;

import lombok.Data;
import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * DTO for {@link com.gengzi.sftp.usermodel.dao.audit.StatisticsRecord}
 */
@Data
public class StatisticsTrafficResponse implements Serializable {
    @NotNull
    @Size(max = 64)
    String uploadSize;
    @NotNull
    @Size(max = 64)
    String downloadSize;

    String timeLabel;
}