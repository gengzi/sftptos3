package com.gengzi.sftp.usermodel.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO for {@link com.gengzi.sftp.usermodel.dao.user.entity.User}
 */
@Data
public class AdminInfoUpdateRequest implements Serializable {
    @NotNull
    Long id;
    @NotBlank
    String passwd;

}