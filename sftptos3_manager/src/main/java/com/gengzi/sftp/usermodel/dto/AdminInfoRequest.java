package com.gengzi.sftp.usermodel.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * DTO for {@link com.gengzi.sftp.usermodel.dao.user.entity.User}
 */
@Data
public class AdminInfoRequest implements Serializable {
    @NotBlank
    String username;
    @NotBlank
    String passwd;

}