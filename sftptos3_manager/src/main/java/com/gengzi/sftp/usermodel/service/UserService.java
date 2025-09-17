package com.gengzi.sftp.usermodel.service;


import com.gengzi.sftp.usermodel.dto.UserInfoRequest;
import com.gengzi.sftp.usermodel.dto.UserLoginRequest;
import com.gengzi.sftp.usermodel.response.JwtResponse;

/**
 * 用户service层
 */
public interface UserService {


    JwtResponse longing(UserLoginRequest loginRequest);



    void createUser(UserInfoRequest userInfoRequest);



}
