package com.gengzi.sftp.usermodel.service;


import com.gengzi.sftp.usermodel.dao.user.entity.User;
import com.gengzi.sftp.usermodel.dto.UserInfoRequest;
import com.gengzi.sftp.usermodel.dto.UserInfoUpdateRequest;
import com.gengzi.sftp.usermodel.dto.UserLoginRequest;
import com.gengzi.sftp.usermodel.response.JwtResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;

/**
 * 用户service层
 */
public interface UserService {


    JwtResponse longing(UserLoginRequest loginRequest);



    void createUser(UserInfoRequest userInfoRequest);


    Page<User> list(String username, Pageable pageable);


    void updateUser(@Valid UserInfoUpdateRequest userInfoRequest);

    Boolean removeUser(@Valid Long id);

    User details(@Valid Long id);
}
