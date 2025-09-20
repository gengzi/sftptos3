package com.gengzi.sftp.usermodel.service;


import com.gengzi.sftp.usermodel.dao.user.entity.Admin;
import com.gengzi.sftp.usermodel.dto.AdminInfoRequest;
import com.gengzi.sftp.usermodel.dto.AdminInfoUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;

/**
 * admin service层
 */
public interface AdminService {

    void createUser(AdminInfoRequest userInfoRequest);


    Page<Admin> list(String username, Pageable pageable);


    void updateUser(@Valid AdminInfoUpdateRequest userInfoRequest);


    Boolean removeUser(@Valid Long id);

}
