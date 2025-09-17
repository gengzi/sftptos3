package com.gengzi.sftp.usermodel.dto;

import lombok.Data;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * DTO for {@link com.gengzi.sftp.usermodel.dao.user.entity.User}
 */
@Data
public class UserInfoRequest implements Serializable {
    @NotBlank
    String username;
    @NotBlank
    String passwd;
    @NotBlank
    String userRootPath;
    @NotBlank
    String accessStorageType;
    @NotBlank
    String accessStorageInfo;
}