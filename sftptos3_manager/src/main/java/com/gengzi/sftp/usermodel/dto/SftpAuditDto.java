package com.gengzi.sftp.usermodel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * DTO for {@link com.gengzi.sftp.dao.SftpAudit}
 */
@Value
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Data
public class SftpAuditDto implements Serializable {
    @NotNull
    @Size(max = 32)
    String type;
    @NotNull
    Byte operateResult;
    @NotNull
    @Size(max = 64)
    String fileSize;

}